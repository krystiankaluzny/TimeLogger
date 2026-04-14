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
    val color: String = "#FF9E9E9E",
    val titlePatterns: List<String> = emptyList()
) {
    companion object {
        val DEFAULT_PALETTE = listOf(
            "#FF4CAF50", "#FF2196F3", "#FFFF9800",
            "#FFE91E63", "#FF9C27B0", "#FF00BCD4"
        )

        fun paletteColorForIndex(index: Int) = DEFAULT_PALETTE[index % DEFAULT_PALETTE.size]
    }
}
