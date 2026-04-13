package org.obywatelgcc.timelogger.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import org.obywatelgcc.timelogger.categories.model.CategoryGroup
import org.obywatelgcc.timelogger.categories.model.CategoryItem
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

// --- Combined add / edit dialog for a category group ---

@Composable
internal fun CategoryGroupDialog(
    group: CategoryGroup?,   // null → add mode, non-null → edit mode
    onAction: (CategoriesAction) -> Unit,
    onDismiss: () -> Unit
) {
    val isAddMode = group == null

    // Title editing state
    var titleText by rememberSaveable { mutableStateOf(group?.name ?: "") }
    var isTitleEditing by rememberSaveable { mutableStateOf(isAddMode) }

    // Nested dialogs shown from within this dialog
    var showAddItemDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CategoryItem?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Clickable title — click to edit inline
            if (isTitleEditing) {
                TextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    singleLine = true,
                    placeholder = { Text("Category name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            // Dispatch rename when focus leaves, if editing an existing group
                            if (!focusState.isFocused && !isAddMode && group != null) {
                                val trimmed = titleText.trim()
                                if (trimmed.isNotBlank() && trimmed != group.name) {
                                    onAction(CategoriesAction.RenameCategory(group.id, trimmed))
                                }
                                isTitleEditing = false
                            }
                        }
                )
            } else {
                Text(
                    text = titleText.ifBlank { "Category name" },
                    style = TimeLoggerTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isTitleEditing = true }
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isAddMode && group != null) {
                    // Delete button for existing group
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete category")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Items", style = TimeLoggerTheme.typography.labelLarge)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(top = 4.dp)
                    ) {
                        items(group.items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
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
                                    onClick = { editingItem = item },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Edit item",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onAction(CategoriesAction.DeleteItem(group.id, item.id)) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete item",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        item {
                            TextButton(onClick = { showAddItemDialog = true }) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Item")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isAddMode) {
                TextButton(
                    onClick = {
                        val name = titleText.trim()
                        if (name.isNotBlank()) {
                            onAction(CategoriesAction.AddCategory(name))
                            onDismiss()
                        }
                    },
                    enabled = titleText.isNotBlank()
                ) {
                    Text("Create")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = if (isAddMode) {
            { TextButton(onClick = onDismiss) { Text("Cancel") } }
        } else null
    )

    // Nested dialogs

    if (showAddItemDialog && group != null) {
        AddNameDialog(
            title = "New Item",
            onConfirm = { name ->
                onAction(CategoriesAction.AddItem(group.id, name))
                showAddItemDialog = false
            },
            onDismiss = { showAddItemDialog = false }
        )
    }

    if (showDeleteConfirm && group != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete \"${group.name}\"?") },
            text = { Text("This will remove the category and all its items.") },
            confirmButton = {
                TextButton(onClick = {
                    onAction(CategoriesAction.DeleteCategory(group.id))
                    showDeleteConfirm = false
                    onDismiss()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    editingItem?.let { item ->
        if (group != null) {
            CategoryItemDialog(
                categoryId = group.id,
                item = item,
                onAction = onAction,
                onDismiss = { editingItem = null }
            )
        }
    }
}

// --- Item edit dialog ---

@Composable
internal fun CategoryItemDialog(
    categoryId: String,
    item: CategoryItem,
    onAction: (CategoriesAction) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddPatternField by rememberSaveable { mutableStateOf(false) }
    var newPatternText by rememberSaveable { mutableStateOf("") }
    var itemNameText by rememberSaveable { mutableStateOf(item.name) }
    var isNameEditing by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(colorFromHex(item.color))
                )
                Spacer(Modifier.width(8.dp))
                if (isNameEditing) {
                    TextField(
                        value = itemNameText,
                        onValueChange = { itemNameText = it },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    val trimmed = itemNameText.trim()
                                    if (trimmed.isNotBlank() && trimmed != item.name) {
                                        onAction(CategoriesAction.RenameItem(categoryId, item.id, trimmed))
                                    }
                                    isNameEditing = false
                                }
                            }
                    )
                } else {
                    Text(
                        text = itemNameText,
                        style = TimeLoggerTheme.typography.titleMedium,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { isNameEditing = true }
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Color palette
                Text("Color", style = TimeLoggerTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryItem.DEFAULT_PALETTE.forEach { colorHex ->
                        ColorSwatch(
                            colorHex = colorHex,
                            isSelected = item.color == colorHex,
                            onClick = { onAction(CategoriesAction.SetItemColor(categoryId, item.id, colorHex)) }
                        )
                    }
                }

                // Patterns
                Text("Title patterns", style = TimeLoggerTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item.titlePatterns.forEach { pattern ->
                        InputChip(
                            selected = false,
                            onClick = {},
                            label = { Text(pattern, style = TimeLoggerTheme.typography.bodySmall) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        onAction(
                                            CategoriesAction.RemovePattern(
                                                categoryId,
                                                item.id,
                                                pattern
                                            )
                                        )
                                    },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = { showAddPatternField = !showAddPatternField },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add pattern", modifier = Modifier.size(18.dp))
                    }
                }

                if (showAddPatternField) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = newPatternText,
                            onValueChange = { newPatternText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Event title pattern") },
                            singleLine = true
                        )
                        TextButton(
                            onClick = {
                                if (newPatternText.isNotBlank()) {
                                    onAction(CategoriesAction.AddPattern(categoryId, item.id, newPatternText.trim()))
                                    newPatternText = ""
                                    showAddPatternField = false
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// --- Generic name input dialog ---

@Composable
internal fun AddNameDialog(
    title: String,
    initialValue: String = "",
    onConfirm: (String) -> Unit,
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
                onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                enabled = text.isNotBlank()
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

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
