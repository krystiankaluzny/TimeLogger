package org.obywatelgcc.timelogger.categories

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.obywatelgcc.timelogger.categories.presentation.category.CategoryState
import org.obywatelgcc.timelogger.core.presentation.components.chart.PieChart
import org.obywatelgcc.timelogger.core.presentation.components.chart.PieChartProperties
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Data
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.DurationScale
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeView
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import java.time.Duration

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
                    .weight(4f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CategorySelectorRow(categoryState, categoriesViewModel::onCategoryAction)
                FilterTimeRangeView(
                    filterState.timeRange,
                    filterState.timeRangeType,
                    categoriesViewModel::onFilterAction
                )
                CategoryLegendView(categoryState, Modifier)
            }
            Column(
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CategoryPieChartView(categoryState, Modifier.padding(top = 0.dp, bottom = 30.dp))
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CategorySelectorRow(categoryState, categoriesViewModel::onCategoryAction)
            FilterTimeRangeView(filterState.timeRange, filterState.timeRangeType, categoriesViewModel::onFilterAction)
            CategoryPieChartView(categoryState, Modifier
                .weight(2f)
                .padding(vertical = 35.dp))
            CategoryLegendView(categoryState, Modifier.weight(1f))
        }
    }
}

// --- Selected category header: tap name to edit, + to add ---

@Composable
private fun CategorySelectorRow(
    state: CategoryState,
    onAction: (CategoriesAction) -> Unit
) {
    var showEditDialog by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(0.9f),
            onClick = { showEditDialog = true }
        ) {
            Text(
                text = state.selectedCategory?.name ?: "No category",
                style = TimeLoggerTheme.typography.titleLarge
            )
        }
    }

    if (showEditDialog) {
        CategoryDialog(
            state = state,
            onAction = onAction,
            onDismiss = { showEditDialog = false }
        )
    }
}

// --- Pie chart ---

@Composable
private fun CategoryPieChartView(state: CategoryState, modifier: Modifier) {
    val data = state.itemStats
        .filter { !it.duration.isZero }
        .map { stat ->
            Data(
                label = stat.itemName,
                value = stat.duration,
                color = colorFromHex(stat.colorHex)
            )
        }

    if (data.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("No data")
        }
    } else {
        PieChart(
            modifier = modifier.fillMaxWidth(),
            data = data,
            scale = DurationScale(domain = DurationScale.totalDurationDomain(data)),
            properties = PieChartProperties()
        )
    }
}

// --- Legend ---

@Composable
private fun CategoryLegendView(state: CategoryState, modifier: Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(state.itemStats) { stat ->
            LegendRow(colorFromHex(stat.colorHex), stat.itemName, stat.duration)
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
        Text(text = name, modifier = Modifier.weight(1f), style = TimeLoggerTheme.typography.bodyMedium)
        Text(text = duration.toHoursMinutesString(), style = TimeLoggerTheme.typography.bodyMedium)
    }
}

private fun Duration.toHoursMinutesString(): String = "${toHours()}h ${toMinutesPart()}m"
