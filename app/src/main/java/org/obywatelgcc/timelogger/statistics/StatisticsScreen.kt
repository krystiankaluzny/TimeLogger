package org.obywatelgcc.timelogger.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.obywatelgcc.timelogger.core.presentation.components.CalendarDropdown

@Composable
fun StatisticsScreen() {

    val viewModel = koinViewModel<StatisticsViewModel>()
    val calendarState by viewModel.statisticsCalendarState.collectAsStateWithLifecycle()

    val calendars = calendarState.availableCalendars
    val selectedCalendar = calendarState.selectedCalendar

    val initialized by viewModel.initialized.collectAsStateWithLifecycle()
    if (initialized) {
        CalendarDropdown(calendars, selectedCalendar, {  })
    }

}