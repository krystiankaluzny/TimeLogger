package org.obywatelgcc.timelogger.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import org.obywatelgcc.timelogger.categories.model.Category
import org.obywatelgcc.timelogger.categories.model.CategoryItem
import org.obywatelgcc.timelogger.categories.presentation.category.CategoryState
import org.obywatelgcc.timelogger.core.presentation.components.ColorButton
import org.obywatelgcc.timelogger.core.presentation.components.TextField
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryDialog(
    state: CategoryState,
    onAction: (CategoriesAction) -> Unit,
    onDismiss: () -> Unit
) {
    val category = state.selectedCategory

    // Nested dialogs shown from within this dialog
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var showEditCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteCategoryConfirmDialog by rememberSaveable { mutableStateOf(false) }

    var showAddItemDialog by rememberSaveable { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<CategoryItem?>(null) }
    var itemToDelete by remember { mutableStateOf<CategoryItem?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = TimeLoggerTheme.shapes.medium,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // --- Top row: combo-box ---
                CategoryTitleComboBox(
                    modifier = Modifier.fillMaxWidth(),
                    selectedCategoryTitle = category?.name,
                    categories = state.categories,
                    onCategorySelected = { category ->
                        onAction(CategoriesAction.SelectCategory(category))
                    }
                )

                // --- Items list (visible when a category is selected) ---
                category?.let {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(top = 4.dp)
                    ) {
                        items(category.items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(colorFromHex(item.color))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.name,
                                    modifier = Modifier.weight(1f),
                                    style = TimeLoggerTheme.typography.bodyMedium
                                )

                                IconButton(
                                    onClick = { itemToEdit = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Edit item",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { itemToDelete = item; },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete item",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        item {
                            TextButton(onClick = { showAddItemDialog = true }) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(text = "Add Item")
                            }
                        }
                    }
                }

                // --- Buttons row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.weight(1f)) {

                        IconButton(onClick = { showAddCategoryDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add category"
                            )
                        }

                        IconButton(
                            onClick = { showEditCategoryDialog = true },
                            enabled = category != null
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit category"
                            )
                        }

                        IconButton(
                            onClick = { showDeleteCategoryConfirmDialog = true },
                            enabled = category != null
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete category"
                            )
                        }
                    }

                    TextButton(onClick = onDismiss) { Text(text = "Close") }
                }
            }
        }
    }

    // Nested dialogs

    if (showAddCategoryDialog) {
        NameActionDialog(
            title = "New Item",
            onNameConfirmed = { name ->
                onAction(CategoriesAction.AddCategory(name))
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    category?.let {
        if (showEditCategoryDialog) {
            NameActionDialog(
                title = category.name,
                initialValue = category.name,
                onNameConfirmed = { name ->
                    onAction(CategoriesAction.RenameCategory(category.id, name))
                    showEditCategoryDialog = false
                },
                onDismiss = { showEditCategoryDialog = false }
            )
        }

        if (showDeleteCategoryConfirmDialog) {
            DeleteActionDialog(
                title = category.name,
                text = "This will remove the category and all its items.",
                onConfirm = { onAction(CategoriesAction.DeleteCategory(category.id)) },
                onDismiss = { showDeleteCategoryConfirmDialog = false }
            )
        }

        if (showAddItemDialog) {
            NameActionDialog(
                title = "New Item",
                onNameConfirmed = { name ->
                    onAction(CategoriesAction.AddItem(category.id, name))
                    showAddItemDialog = false
                },
                onDismiss = { showAddItemDialog = false }
            )
        }

        itemToEdit?.let { item ->
            CategoryItemDialog(
                categoryId = category.id,
                sourceItem = item,
                allItems = category.items,
                eventTitles = state.eventTitles,
                onAction = onAction,
                onDismiss = { itemToEdit = null }
            )
        }

        itemToDelete?.let { item ->
            DeleteActionDialog(
                title = item.name,
                text = "This will remove the item.",
                onConfirm = { onAction(CategoriesAction.DeleteItem(category.id, item.id)) },
                onDismiss = { itemToDelete = null }
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTitleComboBox(
    modifier: Modifier,
    selectedCategoryTitle: String?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = dropdownExpanded,
        onExpandedChange = { dropdownExpanded = it }
    ) {
        TextField(
            value = selectedCategoryTitle ?: "",
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            placeholder = { Text(text = "Category name", style = TimeLoggerTheme.typography.bodyMedium) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
            textStyle = TimeLoggerTheme.typography.bodyMedium,
            modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category)
                        dropdownExpanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun NameActionDialog(
    title: String,
    initialValue: String = "",
    onNameConfirmed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by rememberSaveable { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text("Name") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onNameConfirmed(text.trim()) },
                enabled = text.isNotBlank()
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeleteActionDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete \"${title}\"?") },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) { Text(text = "Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        }
    )
}

// --- Item edit dialog ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryItemDialog(
    categoryId: String,
    sourceItem: CategoryItem,
    allItems: List<CategoryItem>,
    eventTitles: Set<String>,
    onAction: (CategoriesAction) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7",
        "#2196F3", "#00BCD4", "#4CAF50", "#FF9800"
    )


    var name by rememberSaveable { mutableStateOf(sourceItem.name) }
    var color by rememberSaveable { mutableStateOf(palette.find { it == sourceItem.color } ?: palette.random()) }
    var titlePatterns by rememberSaveable { mutableStateOf(sourceItem.titlePatterns) }

    var allTitlePatterns by rememberSaveable { mutableStateOf(allItems.map { it.titlePatterns }.flatten()) }
    var availableTitles by rememberSaveable { mutableStateOf(eventTitles - allTitlePatterns) }
    var titleSuggestions by remember { mutableStateOf(emptyList<String>()) }

    var newPatternTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    var titleSuggestionsExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = TimeLoggerTheme.shapes.medium,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TextField(
                    modifier = Modifier.height(48.dp),
                    textStyle = TimeLoggerTheme.typography.bodyMedium,
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    palette.forEach { hex ->
                        ColorButton(
                            color = colorFromHex(hex),
                            modifier = Modifier
                                .size(24.dp),
                            onClick = { color = hex },
                            showBorder = (hex == color)
                        )
                    }
                }

                Text(text = "Title patterns", style = TimeLoggerTheme.typography.labelLarge)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ExposedDropdownMenuBox(
                        expanded = titleSuggestionsExpanded,
                        onExpandedChange = { titleSuggestionsExpanded = it },
                        modifier = Modifier
                            .weight(1.0f)
                    ) {

                        TextField(
                            value = newPatternTextFieldValue,
                            onValueChange = {
                                newPatternTextFieldValue = it
                                titleSuggestions = availableTitles.filter { title ->
                                    title.contains(
                                        newPatternTextFieldValue.text,
                                        ignoreCase = true
                                    )
                                }
                                titleSuggestionsExpanded = true
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .menuAnchor(MenuAnchorType.PrimaryEditable),
                            placeholder = { Text("Event title pattern") },
                            singleLine = true,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        )

                        if (titleSuggestions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = titleSuggestionsExpanded,
                                onDismissRequest = { titleSuggestionsExpanded = false }
                            ) {
                                titleSuggestions.forEach { suggestion ->
                                    DropdownMenuItem(
                                        onClick = {
                                            newPatternTextFieldValue =
                                                TextFieldValue(suggestion, selection = TextRange(suggestion.length))
                                            titleSuggestionsExpanded = false
                                        },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {

                                                Text(
                                                    text = suggestion,
                                                    fontSize = TimeLoggerTheme.values.textFieldFontSize,
                                                    modifier = Modifier
                                                        .weight(1.0f)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }

                    }

                    //TODO ADD pattern

                    IconButton(
                        onClick = {
                            val newPatternText = newPatternTextFieldValue.text.trim()
                            if (newPatternText.isNotBlank()) {
                                titlePatterns += newPatternText
                                allTitlePatterns += newPatternText
                                availableTitles -= newPatternText
                                titleSuggestions = emptyList()
                                newPatternTextFieldValue = TextFieldValue("")
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add pattern", modifier = Modifier.size(18.dp))
                    }


                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy((-10).dp),
                ) {
                    titlePatterns.forEach { pattern ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(pattern, style = TimeLoggerTheme.typography.bodySmall) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        titlePatterns -= pattern
                                        allTitlePatterns -= pattern
                                        availableTitles += pattern
                                    },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                // --- Buttons row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {

                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.weight(1f)) {
                        TextButton(onClick = onDismiss) { Text(text = "Close") }
                    }
                    //TODO change detection, so when Close button is clicked then we show allert dialog


                    TextButton(onClick = {}) { Text(text = "Save") }
                }
            }
        }
    }
}

// --- Generic name input dialog ---

// --- Color swatch ---

@Composable
internal fun ColorSwatch(colorHex: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(if (isSelected) 22.dp else 16.dp)
            .clip(CircleShape)
            .background(colorFromHex(colorHex))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    )
}

// --- Shared utility ---

internal fun colorFromHex(hex: String): Color = try {
    Color(hex.toColorInt())
} catch (_: IllegalArgumentException) {
    Color.Gray
}
