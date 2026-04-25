package org.obywatelgcc.timelogger.categories.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
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
    }
}
