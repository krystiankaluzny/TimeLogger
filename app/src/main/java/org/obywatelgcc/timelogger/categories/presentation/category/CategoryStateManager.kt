package org.obywatelgcc.timelogger.categories.presentation.category

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.categories.model.CategoryGroup
import org.obywatelgcc.timelogger.categories.model.CategoryItem
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.CalendarEvent
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID

class CategoryStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    initialState: CategoryState
) : BaseStateManager<CategoryState>(
    coroutineScope,
    savedStateHandle,
    dataStoreManager,
    initialState
) {

    private val categoryPreferences =
        jsonDataStoreStateFlow("categoryPreferences_v1", CategoryPreferences())

    suspend fun init() {
        categoryPreferences.loadFormDataStore()
        state.update { it.copy(categories = categoryPreferences.value.groups) }
    }

    // --- Category CRUD ---

    fun addCategory(name: String) {
        val newGroup = CategoryGroup(id = UUID.randomUUID().toString(), name = name)
        mutateGroups { it + newGroup }
    }

    fun renameCategory(categoryId: String, newName: String) {
        mutateGroups { groups ->
            groups.map { if (it.id == categoryId) it.copy(name = newName) else it }
        }
    }

    fun deleteCategory(categoryId: String) {
        mutateGroups { groups -> groups.filter { it.id != categoryId } }
    }

    fun selectCategory(index: Int) {
        state.update {
            it.copy(selectedCategoryIndex = index.coerceIn(0, (it.categories.size - 1).coerceAtLeast(0)))
        }
    }

    // --- Item CRUD ---

    fun addItem(categoryId: String, name: String) {
        val newItemIndex = state.value.categories.find { it.id == categoryId }?.items?.size ?: 0
        val newItem = CategoryItem(
            id = UUID.randomUUID().toString(),
            name = name,
            color = CategoryItem.paletteColorForIndex(newItemIndex)
        )
        mutateGroupItems(categoryId) { it + newItem }
    }

    fun renameItem(categoryId: String, itemId: String, newName: String) {
        mutateGroupItems(categoryId) { items ->
            items.map { if (it.id == itemId) it.copy(name = newName) else it }
        }
    }

    fun deleteItem(categoryId: String, itemId: String) {
        mutateGroupItems(categoryId) { items -> items.filter { it.id != itemId } }
    }

    fun setItemColor(categoryId: String, itemId: String, colorHex: String) {
        mutateGroupItems(categoryId) { items ->
            items.map { if (it.id == itemId) it.copy(color = colorHex) else it }
        }
    }

    // --- Pattern management ---

    fun addPattern(categoryId: String, itemId: String, pattern: String) {
        mutateItem(categoryId, itemId) { it.copy(titlePatterns = it.titlePatterns + pattern) }
    }

    fun removePattern(categoryId: String, itemId: String, pattern: String) {
        mutateItem(categoryId, itemId) { it.copy(titlePatterns = it.titlePatterns - pattern) }
    }

    // --- UI state ---

    fun toggleManageMode() {
        state.update { it.copy(isManageMode = !it.isManageMode) }
    }

    // --- Statistics calculation ---

    fun recalculate(queryTimeRange: ZonedDateTimeRange, events: List<CalendarEvent>) {
        val category = state.value.selectedCategory ?: run {
            state.update { it.copy(itemStats = emptyList(), unmatchedDuration = Duration.ZERO) }
            return
        }

        val durationMap = mutableMapOf<String, Duration>()
        category.items.forEach { durationMap[it.id] = Duration.ZERO }
        var unmatchedDuration = Duration.ZERO

        for (event in events) {
            val eventDuration = clampedDuration(event.timeRange, queryTimeRange)
            if (eventDuration.isNegative || eventDuration.isZero) continue

            val matchedItem = category.items.firstOrNull { item ->
                item.titlePatterns.any { pattern ->
                    event.title.contains(pattern, ignoreCase = true)
                }
            }

            if (matchedItem != null) {
                durationMap[matchedItem.id] = (durationMap[matchedItem.id] ?: Duration.ZERO) + eventDuration
            } else {
                unmatchedDuration += eventDuration
            }
        }

        val stats = category.items.map { item ->
            CategoryState.CategoryItemStat(
                itemId = item.id,
                itemName = item.name,
                colorHex = item.color,
                duration = durationMap[item.id] ?: Duration.ZERO
            )
        }

        state.update { it.copy(itemStats = stats, unmatchedDuration = unmatchedDuration) }
    }

    // --- Private helpers ---

    private fun mutateGroups(transform: (List<CategoryGroup>) -> List<CategoryGroup>) {
        val newGroups = transform(state.value.categories)
        state.update { s ->
            s.copy(
                categories = newGroups,
                selectedCategoryIndex = s.selectedCategoryIndex.coerceIn(
                    0, (newGroups.size - 1).coerceAtLeast(0)
                )
            )
        }
        categoryPreferences.edit { it.copy(groups = newGroups) }
    }

    private fun mutateGroupItems(categoryId: String, transform: (List<CategoryItem>) -> List<CategoryItem>) {
        mutateGroups { groups ->
            groups.map { group ->
                if (group.id == categoryId) group.copy(items = transform(group.items)) else group
            }
        }
    }

    private fun mutateItem(categoryId: String, itemId: String, transform: (CategoryItem) -> CategoryItem) {
        mutateGroupItems(categoryId) { items ->
            items.map { if (it.id == itemId) transform(it) else it }
        }
    }

    private fun clampedDuration(eventRange: ZonedDateTimeRange, queryRange: ZonedDateTimeRange): Duration {
        val from = maxOf(eventRange.from, queryRange.from)
        val to = minOf(eventRange.to, queryRange.to)
        return Duration.between(from, to)
    }

}

@Serializable
private data class CategoryPreferences(
    val groups: List<CategoryGroup> = emptyList()
)
