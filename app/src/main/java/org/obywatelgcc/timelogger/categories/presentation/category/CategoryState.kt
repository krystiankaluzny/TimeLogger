package org.obywatelgcc.timelogger.categories.presentation.category

import org.obywatelgcc.timelogger.categories.model.Category
import java.time.Duration

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val itemStats: List<CategoryItemStat> = emptyList(),
    val unmatchedDuration: Duration = Duration.ZERO
) {
    data class CategoryItemStat(
        val itemId: String,
        val itemName: String,
        val colorHex: String,
        val duration: Duration
    )

    val selectedCategory: Category?
        get() = categories.getOrNull(selectedCategoryIndex)

    val totalDuration: Duration
        get() = itemStats.fold(Duration.ZERO) { acc, s -> acc + s.duration } + unmatchedDuration
}
