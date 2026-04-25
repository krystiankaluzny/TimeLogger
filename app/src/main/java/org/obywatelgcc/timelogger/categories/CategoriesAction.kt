package org.obywatelgcc.timelogger.categories

import org.obywatelgcc.timelogger.categories.model.Category
import org.obywatelgcc.timelogger.categories.model.CategoryItem

sealed interface CategoriesAction {
    data class SelectCategory(val category: Category) : CategoriesAction
    data class AddCategory(val name: String) : CategoriesAction
    data class RenameCategory(val categoryId: String, val newName: String) : CategoriesAction
    data class DeleteCategory(val categoryId: String) : CategoriesAction

    data class AddItem(val categoryId: String, val name: String) : CategoriesAction
    data class RenameItem(val categoryId: String, val itemId: String, val newName: String) : CategoriesAction
    data class DeleteItem(val categoryId: String, val itemId: String) : CategoriesAction

    data class UpdateItem(val categoryId: String, val item: CategoryItem) : CategoriesAction
}
