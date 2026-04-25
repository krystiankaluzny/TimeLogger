package org.obywatelgcc.timelogger.categories.presentation.category

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.categories.model.Category
import org.obywatelgcc.timelogger.categories.model.CategoryItem
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.CalendarEvent
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.utils.logDebug
import java.time.Duration
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
        jsonDataStoreStateFlow("categoryPreferences_v2", CategoryPreferences())

    suspend fun init() {
        categoryPreferences.loadFormDataStore()
        state.update { it.copy(categories = categoryPreferences.value.categories) }
    }

    // --- Category CRUD ---

    fun addCategory(name: String) {
        val newCategory = Category(id = UUID.randomUUID().toString(), name = name)
        mutateCategories { it + newCategory }
    }

    fun renameCategory(categoryId: String, newName: String) {
        mutateCategories { categories ->
            categories.map { if (it.id == categoryId) it.copy(name = newName) else it }
        }
    }

    fun deleteCategory(categoryId: String) {
        mutateCategories { groups -> groups.filter { it.id != categoryId } }
    }

    fun selectCategory(category: Category) {
        state.update { it.copy(selectedCategory = category) }
        categoryPreferences.edit { it.copy(selectedCategoryId = category.id) }
    }

    // --- Item CRUD ---

    fun addItem(categoryId: String, name: String) {
        val newItemIndex = state.value.categories.find { it.id == categoryId }?.items?.size ?: 0
        val newItem = CategoryItem(
            id = UUID.randomUUID().toString(),
            name = name,
            color = CategoryItem.paletteColorForIndex(newItemIndex)
        )
        mutateCategoryItems(categoryId) { it + newItem }
    }

    fun renameItem(categoryId: String, itemId: String, newName: String) {
        mutateCategoryItems(categoryId) { items ->
            items.map { if (it.id == itemId) it.copy(name = newName) else it }
        }
    }

    fun deleteItem(categoryId: String, itemId: String) {
        mutateCategoryItems(categoryId) { items -> items.filter { it.id != itemId } }
    }

    fun setItemColor(categoryId: String, itemId: String, colorHex: String) {
        mutateCategoryItems(categoryId) { items ->
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
                    event.title.equals(pattern, ignoreCase = true)
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

        val eventTitles = events
            .map { it -> it.title }
            .toSet()

        logDebug("eventTitles: $eventTitles")

        state.update { it.copy(itemStats = stats, eventTitles = eventTitles, unmatchedDuration = unmatchedDuration) }
    }

    // --- Private helpers ---

    private fun mutateCategories(transform: (List<Category>) -> List<Category>) {
        val newCategories = transform(state.value.categories)
        state.update { s ->
            s.copy(
                categories = newCategories,
                selectedCategory = newCategories.find { it.id == s.selectedCategory?.id } ?: newCategories.firstOrNull()
            )
        }

        categoryPreferences.edit {
            it.copy(
                selectedCategoryId = state.value.selectedCategory?.id ?: "",
                categories = state.value.categories
            )
        }
    }

    private fun mutateCategoryItems(categoryId: String, transform: (List<CategoryItem>) -> List<CategoryItem>) {
        mutateCategories { categories ->
            categories.map { category ->
                if (category.id == categoryId) category.copy(items = transform(category.items)) else category
            }
        }
    }

    private fun mutateItem(categoryId: String, itemId: String, transform: (CategoryItem) -> CategoryItem) {
        mutateCategoryItems(categoryId) { items ->
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
    val selectedCategoryId: String = "",
    val categories: List<Category> = emptyList()
)
