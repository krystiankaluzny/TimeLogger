package org.obywatelgcc.timelogger.categories.presentation.category

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.categories.model.Category
import org.obywatelgcc.timelogger.categories.model.CategoryItem
import org.obywatelgcc.timelogger.categories.presentation.category.CategoryState.CategoryItemStat
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.CalendarEvent
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
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
        state.update {
            val prefValue = categoryPreferences.value
            it.copy(
                selectedCategory = prefValue.categories.find { it.id == prefValue.selectedCategoryId },
                categories = prefValue.categories
            )
        }
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

    fun showUncategorized(categoryId: String, showUncategorized: Boolean) {
        mutateCategory(categoryId) { it.copy(showUncategorized = showUncategorized) }
    }

    fun selectCategory(category: Category) {
        state.update { it.copy(selectedCategory = category) }
        categoryPreferences.edit { it.copy(selectedCategoryId = category.id) }
    }

    // --- Item CRUD ---

    fun addItem(categoryId: String, name: String) {
        val newItem = CategoryItem(
            id = UUID.randomUUID().toString(),
            name = name,
            color = CategoryItem.DEFAULT_COLOR
        )
        mutateCategoryItems(categoryId) { it + newItem }
    }

    fun updateItem(categoryId: String, item: CategoryItem) {
        mutateItem(categoryId, item.id) { item }
    }

    fun renameItem(categoryId: String, itemId: String, newName: String) {
        mutateCategoryItems(categoryId) { items ->
            items.map { if (it.id == itemId) it.copy(name = newName) else it }
        }
    }

    fun deleteItem(categoryId: String, itemId: String) {
        mutateCategoryItems(categoryId) { items -> items.filter { it.id != itemId } }
    }

    // --- Statistics calculation ---

    fun recalculate(queryTimeRange: ZonedDateTimeRange, events: List<CalendarEvent>) {
        val category = state.value.selectedCategory ?: run {
            state.update { it.copy(itemStats = emptyList(), totalDuration = Duration.ZERO) }
            return
        }

        val durationMap = mutableMapOf<String, Duration>()
        category.items.forEach { durationMap[it.id] = Duration.ZERO }
        var unmatchedDuration = Duration.ZERO
        var totalDuration = Duration.ZERO

        for (event in events) {
            val eventDuration = clampedDuration(event.timeRange, queryTimeRange)
            if (eventDuration.isNegative || eventDuration.isZero) continue

            val matchedItem = category.items.firstOrNull { item ->
                item.titlePatterns.any { pattern ->
                    event.title.trim().equals(pattern, ignoreCase = true)
                }
            }

            if (matchedItem != null) {
                durationMap[matchedItem.id] = (durationMap[matchedItem.id] ?: Duration.ZERO) + eventDuration
                totalDuration += eventDuration
            } else {
                unmatchedDuration += eventDuration
            }
        }

        val stats = category.items
            .map { item ->
                CategoryItemStat(
                    itemId = item.id,
                    itemName = item.name,
                    colorHex = item.color,
                    duration = durationMap[item.id] ?: Duration.ZERO
                )
            }
            .toMutableList()

        if (category.showUncategorized && unmatchedDuration > Duration.ZERO) {
            totalDuration += unmatchedDuration

            stats.add(
                CategoryItemStat(
                    itemId = CategoryItem.UNCATEGORIZED_ID,
                    itemName = CategoryItem.UNCATEGORIZED_NAME,
                    colorHex = CategoryItem.UNCATEGORIZED_COLOR,
                    duration = unmatchedDuration
                )
            )
        }
        val eventTitles = events
            .map { it -> it.title.trim() }
            .toSet()

        state.update {
            it.copy(
                itemStats = stats.toList(),
                eventTitles = eventTitles,
                totalDuration = totalDuration
            )
        }
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

    private fun mutateCategory(categoryId: String, transform: (Category) -> Category) {
        mutateCategories { categories ->
            categories.map { if (it.id == categoryId) transform(it) else it }
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
