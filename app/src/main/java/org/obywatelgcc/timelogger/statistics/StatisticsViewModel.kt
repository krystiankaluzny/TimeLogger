package org.obywatelgcc.timelogger.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.CalendarRepository
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.statistics.presentation.calendar.StatisticsCalendarState
import org.obywatelgcc.timelogger.statistics.presentation.calendar.StatisticsCalendarStateManager
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsState
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsStateManager
import org.obywatelgcc.timelogger.utils.logDebug
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class StatisticsViewModel(
    savedStateHandle: SavedStateHandle,
    dataSoreManager: DataStoreManager,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val statisticsCalendarStateManager =
        StatisticsCalendarStateManager(viewModelScope, savedStateHandle, dataSoreManager, StatisticsCalendarState())
    private val statisticsStateManager =
        StatisticsStateManager(viewModelScope, savedStateHandle, dataSoreManager, StatisticsState())

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized
        .onStart { initData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val statisticsCalendarState = statisticsCalendarStateManager.state.asStateFlow()
    val statisticsState = statisticsStateManager.state.asStateFlow()

    private suspend fun initData() {
        logDebug("initData")
        statisticsCalendarStateManager.init(
            calendarRepository.findAllCalendars()
        )
        _initialized.update { true }

        onAction(StatisticsAction.SelectCalendar(statisticsCalendarState.value.selectedCalendar))
    }

    fun onAction(action: StatisticsAction) = when (action) {
        is StatisticsAction.SelectCalendar -> {
            statisticsCalendarStateManager.selectCalendar(action.calendar)

            val now = ZonedDateTime.now()
            val beginningOfDay = now.truncatedTo(ChronoUnit.DAYS).minusDays(1L)

            val queryTimeRange = ZonedDateTimeRange(beginningOfDay, now)
            viewModelScope.launch {
                val events = calendarRepository.findEventsInTimeRange(action.calendar, queryTimeRange)
                statisticsStateManager.recalculate(queryTimeRange, events)
            }
        }
    }

}
