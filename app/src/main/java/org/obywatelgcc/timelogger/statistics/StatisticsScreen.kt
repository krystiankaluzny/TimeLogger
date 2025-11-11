package org.obywatelgcc.timelogger.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.components.CalendarDropdown
import org.obywatelgcc.timelogger.core.presentation.components.ToggleButton
import org.obywatelgcc.timelogger.statistics.components.chart.BarChart
import org.obywatelgcc.timelogger.statistics.components.chart.BarChartProperties
import org.obywatelgcc.timelogger.statistics.components.chart.model.Data
import org.obywatelgcc.timelogger.statistics.components.chart.model.DurationScale
import org.obywatelgcc.timelogger.statistics.presentation.filter.FilterTimeRangeType
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsState
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme

@Composable
fun StatisticsScreen() {

    val viewModel = koinViewModel<StatisticsViewModel>()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val statisticsState by viewModel.statisticsState.collectAsStateWithLifecycle()

    val calendars = filterState.availableCalendars
    val selectedCalendar = filterState.selectedCalendar
    val timeRangeType = filterState.timeRangeType
    val timeRange = filterState.timeRange

    val initialized by viewModel.initialized.collectAsStateWithLifecycle()

    if (initialized) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CalendarDropdownView(calendars, selectedCalendar, viewModel::onAction)

            TimeRangeButtonsView(timeRangeType, viewModel::onAction)

            TimeRangeSwapperView(timeRange, viewModel::onAction)

            StatisticsBarChartView(statisticsState)
        }
    }
}

@Composable
private fun CalendarDropdownView(
    calendars: List<Calendar>,
    selectedCalendar: Calendar,
    onAction: (StatisticsAction) -> Unit
) {
    CalendarDropdown(
        calendars,
        selectedCalendar,
        { onAction(StatisticsAction.SelectCalendar(it)) })
}

@Composable
fun TimeRangeButtonsView(
    selectedTimeRangeType: FilterTimeRangeType,
    onAction: (StatisticsAction) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeRangeTypeButton(selectedTimeRangeType, FilterTimeRangeType.DAY, "1D", onAction)
        TimeRangeTypeButton(selectedTimeRangeType, FilterTimeRangeType.WEEK, "1W", onAction)
        TimeRangeTypeButton(selectedTimeRangeType, FilterTimeRangeType.MONTH, "1M", onAction)
    }
}

@Composable
private fun TimeRangeTypeButton(
    selectedTimeRangeType: FilterTimeRangeType,
    targetTimeRangeType: FilterTimeRangeType,
    text: String,
    onAction: (StatisticsAction) -> Unit
) {
    ToggleButton(
        modifier = Modifier.padding(horizontal = 5.dp),
        checked = (selectedTimeRangeType == targetTimeRangeType),
        onCheckedChange = {
            if (it) {
                onAction(StatisticsAction.SelectTimeRangeType(targetTimeRangeType))
            }
        }
    ) { Text(text = text) }
}

//TODO zmienić nazwę
@Composable
fun TimeRangeSwapperView(
    timeRange: ZonedDateTimeRange,
    onAction: (StatisticsAction) -> Unit
) {

}

@Composable
fun StatisticsBarChartView(statisticsState: StatisticsState) {
    val statisticItems = statisticsState.statisticItems
    val data = statisticItems
        .map {
            Data(
                label = it.title,
                value = it.totalDuration,
                color = it.color?.colorAsLong()?.let { Color(it) } ?: Color.White
            )
        }

    if (data.isNotEmpty()) {
        BarChart(
            modifier = Modifier
                .padding(horizontal = 22.dp),
            data = data,
            scale = DurationScale(),
            properties = BarChartProperties.of(
                textColor = TimeLoggerTheme.colorScheme.onSurface,
                axisLineColor = TimeLoggerTheme.colorScheme.onSurface
            ).copy(
                barValueTextInsideBar = false
            )
        )
    } else {
        Text(text = "No data")
    }
}