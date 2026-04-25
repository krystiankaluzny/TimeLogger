package org.obywatelgcc.timelogger.categories.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val showUncategorized: Boolean = false,
    val items: List<CategoryItem> = emptyList()
)

@Serializable
data class CategoryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String = DEFAULT_COLOR,
    val titlePatterns: List<String> = emptyList()
) {
    companion object {
        const val DEFAULT_COLOR = "#F44336"
        const val UNCATEGORIZED_NAME = "Uncategorized"
        const val UNCATEGORIZED_ID = "UncategorizedId"
        const val UNCATEGORIZED_COLOR = "#9E9E9E"
    }
}
