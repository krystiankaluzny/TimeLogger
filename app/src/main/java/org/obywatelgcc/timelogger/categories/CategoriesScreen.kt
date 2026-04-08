package org.obywatelgcc.timelogger.categories

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.obywatelgcc.timelogger.categories.model.CategoryItem
import org.obywatelgcc.timelogger.categories.presentation.category.CategoryState
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.components.OutlinedTextField
import org.obywatelgcc.timelogger.core.presentation.components.chart.PieChart
import org.obywatelgcc.timelogger.core.presentation.components.chart.PieSlice
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeAction
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeType
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeView
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import java.time.Duration
import java.time.format.DateTimeFormatter

@Composable
fun CategoriesScreen(categoriesViewModel: CategoriesViewModel) {
    val filterState by categoriesViewModel.filterState.collectAsStateWithLifecycle()
    val categoryState by categoriesViewModel.categoryState.collectAsStateWithLifecycle()
    val initialized by categoriesViewModel.initialized.collectAsStateWithLifecycle()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (!initialized) return

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CategoryTabsRow(categoryState, categoriesViewModel::onCategoryAction)
                FilterTimeRangeView(filterState.timeRange, filterState.timeRangeType, categoriesViewModel::onFilterAction)
                CategoryLegendView(categoryState, Modifier.weight(1f))
            }
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CategoryPieChartView(categoryState, Modifier.weight(1f))
                AnimatedVisibility(visible = categoryState.isManageMode) {
                    CategoryManagePanel(categoryState, categoriesViewModel::onCategoryAction)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CategoryTabsRow(categoryState, categoriesViewModel::onCategoryAction)
            FilterTimeRangeView(filterState.timeRange, filterState.timeRangeType, categoriesViewModel::onFilterAction)
            CategoryPieChartView(categoryState, Modifier.height(240.dp))
            CategoryLegendView(categoryState, Modifier.weight(1f))
            AnimatedVisibility(visible = categoryState.isManageMode) {
                CategoryManagePanel(categoryState, categoriesViewModel::onCategoryAction)
            }
        }
    }
}

@Composable
private fun CategoryTabsRow(
    state: CategoryState,
    onAction: (CategoriesAction) -> Unit
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        state.categories.forEachIndexed { index, group ->
            FilterChip(
                modifier = Modifier.padding(horizontal = 4.dp),
                selected = index == state.selectedCategoryIndex,
                onClick = { onAction(CategoriesAction.SelectCategory(index)) },
                label = { Text(group.name) }
            )
        }
        IconButton(onClick = { showAddDialog = true }) {
            Icon(Icons.Filled.Add, contentDescription = "Add Category")
        }
        IconButton(onClick = { onAction(CategoriesAction.ToggleManageMode) }) {
            Icon(Icons.Filled.Edit, contentDescription = "Manage")
        }
    }

    if (showAddDialog) {
        AddNameDialog(
            title = "New Category",
            onConfirm = { name ->
                onAction(CategoriesAction.AddCategory(name))
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun CategoryPieChartView(state: CategoryState, modifier: Modifier) {
    val slices = buildList {
        state.itemStats.filter { !it.duration.isZero }.forEach { stat ->
            add(PieSlice(
                label = stat.itemName,
                value = stat.duration.toSeconds().toFloat(),
                color = colorFromHex(stat.colorHex)
            ))
        }
        if (!state.unmatchedDuration.isZero) {
            add(PieSlice("Other", state.unmatchedDuration.toSeconds().toFloat(), Color.Gray))
        }
    }

    if (slices.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("No data")
        }
    } else {
        PieChart(modifier = modifier.fillMaxWidth(), slices = slices)
    }
}

@Composable
private fun CategoryLegendView(state: CategoryState, modifier: Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        itemsIndexed(state.itemStats) { _, stat ->
            LegendRow(
                color = colorFromHex(stat.colorHex),
                name = stat.itemName,
                duration = stat.duration
            )
        }
        if (!state.unmatchedDuration.isZero) {
            item {
                LegendRow(
                    color = Color.Gray,
                    name = "Other",
                    duration = state.unmatchedDuration
                )
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, name: String, duration: Duration) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = TimeLoggerTheme.typography.bodyMedium
        )
        Text(
            text = duration.toHoursMinutesString(),
            style = TimeLoggerTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CategoryManagePanel(
    state: CategoryState,
    onAction: (CategoriesAction) -> Unit
) {
    val category = state.selectedCategory ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        HorizontalDivider()
        Text(
            text = category.name,
            style = TimeLoggerTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(category.items) { _, item ->
                CategoryItemManageRow(
                    categoryId = category.id,
                    item = item,
                    onAction = onAction
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
            item {
                var showAddItemDialog by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item")
                }
                if (showAddItemDialog) {
                    AddNameDialog(
                        title = "New Item",
                        onConfirm = { name ->
                            onAction(CategoriesAction.AddItem(category.id, name))
                            showAddItemDialog = false
                        },
                        onDismiss = { showAddItemDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItemManageRow(
    categoryId: String,
    item: CategoryItem,
    onAction: (CategoriesAction) -> Unit
) {
    var showRenameDialog by rememberSaveable(item.id) { mutableStateOf(false) }
    var showAddPatternField by rememberSaveable(item.id) { mutableStateOf(false) }
    var newPatternText by rememberSaveable(item.id) { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(colorFromHex(item.color))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                style = TimeLoggerTheme.typography.bodyMedium
            )
            IconButton(onClick = { showRenameDialog = true }) {
                Icon(Icons.Filled.Edit, contentDescription = "Rename", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { onAction(CategoriesAction.DeleteItem(categoryId, item.id)) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
            }
        }

        // Color palette
        Row(
            modifier = Modifier.padding(start = 22.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CategoryItem.DEFAULT_PALETTE.forEach { colorHex ->
                val isSelected = item.color == colorHex
                ColorSwatch(
                    colorHex = colorHex,
                    isSelected = isSelected,
                    onClick = { onAction(CategoriesAction.SetItemColor(categoryId, item.id, colorHex)) }
                )
            }
        }

        // Patterns
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, top = 4.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item.titlePatterns.forEach { pattern ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text(pattern, style = TimeLoggerTheme.typography.bodySmall) },
                    trailingIcon = {
                        IconButton(
                            onClick = { onAction(CategoriesAction.RemovePattern(categoryId, item.id, pattern)) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove pattern", modifier = Modifier.size(14.dp))
                        }
                    },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            IconButton(onClick = { showAddPatternField = !showAddPatternField }) {
                Icon(Icons.Filled.Add, contentDescription = "Add pattern", modifier = Modifier.size(18.dp))
            }
        }

        if (showAddPatternField) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 4.dp),
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

    if (showRenameDialog) {
        AddNameDialog(
            title = "Rename Item",
            initialValue = item.name,
            onConfirm = { newName ->
                onAction(CategoriesAction.RenameItem(categoryId, item.id, newName))
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }
}

@Composable
private fun AddNameDialog(
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
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorSwatch(colorHex: String, isSelected: Boolean, onClick: () -> Unit) {
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

private fun colorFromHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    Color.Gray
}

private fun Duration.toHoursMinutesString(): String = "${toHours()}h ${toMinutesPart()}m"
