package org.obywatelgcc.timelogger.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.components.CalendarDropdown
import org.obywatelgcc.timelogger.core.presentation.components.OutlinedTextField
import org.obywatelgcc.timelogger.statistics.StatisticsAction.NextRange
import org.obywatelgcc.timelogger.statistics.StatisticsAction.PreviousRange
import org.obywatelgcc.timelogger.statistics.StatisticsAction.ResetRange
import org.obywatelgcc.timelogger.statistics.StatisticsAction.SelectCalendar
import org.obywatelgcc.timelogger.statistics.StatisticsAction.SelectTimeRangeType
import org.obywatelgcc.timelogger.statistics.components.chart.BarChart
import org.obywatelgcc.timelogger.statistics.components.chart.BarChartProperties
import org.obywatelgcc.timelogger.statistics.components.chart.model.Data
import org.obywatelgcc.timelogger.statistics.components.chart.model.DurationScale
import org.obywatelgcc.timelogger.statistics.presentation.filter.FilterTimeRangeType
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsState
import org.obywatelgcc.timelogger.ui.theme.TimeLoggerTheme
import java.time.Duration
import java.time.format.DateTimeFormatter

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

            TimeRangeValeView(timeRange, timeRangeType, viewModel::onAction)

            StatisticsInfoView(statisticsState)

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
        Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        calendars,
        selectedCalendar,
        { onAction(SelectCalendar(it)) })
}

@Composable
fun TimeRangeButtonsView(
    selectedTimeRangeType: FilterTimeRangeType,
    onAction: (StatisticsAction) -> Unit
) {

    ConstraintLayout {

        val (homeIcon, timeRangeTypes) = createRefs()

        IconButton(
            modifier = Modifier.constrainAs(homeIcon, {
                end.linkTo(timeRangeTypes.start)
                centerVerticallyTo(parent)
            }),
            onClick = { onAction(ResetRange) }) {
            Icon(imageVector = Icons.Filled.Home, contentDescription = "Reset")
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.constrainAs(timeRangeTypes, {
                start.linkTo(homeIcon.end)
                centerVerticallyTo(parent)
                centerHorizontallyTo(parent)
            })
                .height(38.dp)
        ) {
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                onClick = { onAction(SelectTimeRangeType(FilterTimeRangeType.DAY)) },
                selected = selectedTimeRangeType == FilterTimeRangeType.DAY,
                label = { Text(text = "1D") }
            )

            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                onClick = { onAction(SelectTimeRangeType(FilterTimeRangeType.WEEK)) },
                selected = selectedTimeRangeType == FilterTimeRangeType.WEEK,
                label = { Text(text = "1W") }
            )

            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                onClick = { onAction(SelectTimeRangeType(FilterTimeRangeType.MONTH)) },
                selected = selectedTimeRangeType == FilterTimeRangeType.MONTH,
                label = { Text(text = "1M") }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeValeView(
    timeRange: ZonedDateTimeRange,
    timeRangeType: FilterTimeRangeType,
    onAction: (StatisticsAction) -> Unit
) {
    val dateRangeStr = when (timeRangeType) {
        FilterTimeRangeType.DAY -> timeRange.from.format(DateTimeFormatter.ISO_LOCAL_DATE)
        FilterTimeRangeType.WEEK ->
            timeRange.from.format(DateTimeFormatter.ISO_LOCAL_DATE) +
                    " \u2014 " + // em dash ( — )
                    timeRange.to.format(DateTimeFormatter.ISO_LOCAL_DATE)

        FilterTimeRangeType.MONTH -> timeRange.from.format(DateTimeFormatter.ofPattern("yyyy MMMM"))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onAction(PreviousRange) }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(0.75f),
            value = dateRangeStr,
            onValueChange = {},
            readOnly = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )

        IconButton(onClick = { onAction(NextRange) }) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
        }
    }
}


@Composable
fun StatisticsInfoView(statisticsState: StatisticsState) {
    val statisticItems = statisticsState.statisticItems
    val totalDuration = statisticItems.fold(Duration.ZERO) { acc, item -> acc.plus(item.totalDuration) }

    Text(text = "Total: ${totalDuration.toHours()}h ${totalDuration.toMinutesPart()}m")
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
            modifier = Modifier.padding(horizontal = 22.dp),
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