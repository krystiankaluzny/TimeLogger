package org.obywatelgcc.timelogger.categories.presentation.category

import org.obywatelgcc.timelogger.categories.model.Category
import java.time.Duration

data class CategoryState(
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val itemStats: List<CategoryItemStat> = emptyList(),
    val eventTitles: Set<String> = emptySet(),
    val unmatchedDuration: Duration = Duration.ZERO
) {
    data class CategoryItemStat(
        val itemId: String,
        val itemName: String,
        val colorHex: String,
        val duration: Duration
    )
}
