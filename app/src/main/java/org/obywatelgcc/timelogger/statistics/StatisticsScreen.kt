package org.obywatelgcc.timelogger.statistics

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import org.koin.compose.viewmodel.koinViewModel
import org.obywatelgcc.timelogger.core.presentation.components.CalendarDropdown

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


        CalendarDropdown(calendars, selectedCalendar, { it -> viewModel.onAction(StatisticsAction.SelectCalendar(it)) })

        val statisticItems = statisticsState.statisticItems
        val data = statisticItems
            .map {
                Bars(
                    label = it.title,
                    values = listOf(
                        Bars.Data(
                            label = "Statistics",
                            value = it.totalDuration.toMillis().toDouble() / 1000 / 3600,
                            color = SolidColor(it.color?.colorAsLong()?.let { Color(it) } ?: Color.White)
                        ))
                )
            }

//        val data = remember {
//            listOf(
//                Bars(
//                    label = "Jan",
//                    values = listOf(
//                        Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Blue)),
//                        Bars.Data(label = "Windows", value = 70.0, color = SolidColor(Color.Red)),
//                    ),
//                ),
//                Bars(
//                    label = "Feb",
//                    values = listOf(
//                        Bars.Data(label = "Linux", value = 80.0, color =SolidColor(Color.Blue)),
//                        Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red)),
//                    ),
//                )
//            )
//        }

        if(data.isNotEmpty()) {
            RowChart(
                modifier = Modifier
                    .padding(horizontal = 22.dp),
                data = data,
                barProperties = BarProperties(
                    cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, bottomRight = 6.dp),
                    spacing = 3.dp,
                    thickness = 100.dp
                ),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            Text(text = "No data")
        }

        }
    }

}