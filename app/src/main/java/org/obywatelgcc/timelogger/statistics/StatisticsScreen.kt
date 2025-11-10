package org.obywatelgcc.timelogger.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.obywatelgcc.timelogger.core.presentation.components.CalendarDropdown
import org.obywatelgcc.timelogger.statistics.components.chart.BarChart
import org.obywatelgcc.timelogger.statistics.components.chart.model.Data
import org.obywatelgcc.timelogger.statistics.components.chart.model.DurationScale

@Composable
fun StatisticsScreen() {

    val viewModel = koinViewModel<StatisticsViewModel>()
    val calendarState by viewModel.statisticsCalendarState.collectAsStateWithLifecycle()
    val statisticsState by viewModel.statisticsState.collectAsStateWithLifecycle()

    val calendars = calendarState.availableCalendars
    val selectedCalendar = calendarState.selectedCalendar

    val initialized by viewModel.initialized.collectAsStateWithLifecycle()
    if (initialized) {
        Column {


            CalendarDropdown(
                calendars,
                selectedCalendar,
                { it -> viewModel.onAction(StatisticsAction.SelectCalendar(it)) })

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
                    scale = DurationScale()
                )
            } else {
                Text(text = "No data")
            }

        }
    }

}