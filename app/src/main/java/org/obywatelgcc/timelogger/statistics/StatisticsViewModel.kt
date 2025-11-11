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
import org.obywatelgcc.timelogger.statistics.presentation.filter.FilterState
import org.obywatelgcc.timelogger.statistics.presentation.filter.FilterStateManager
import org.obywatelgcc.timelogger.statistics.presentation.filter.FilterTimeRangeType
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsState
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsStateManager
import org.obywatelgcc.timelogger.utils.logDebug
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

class StatisticsViewModel(
    savedStateHandle: SavedStateHandle,
    dataSoreManager: DataStoreManager,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val filterStateManager =
        FilterStateManager(viewModelScope, savedStateHandle, dataSoreManager, FilterState())
    private val statisticsStateManager =
        StatisticsStateManager(viewModelScope, savedStateHandle, dataSoreManager, StatisticsState())

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized
        .onStart { initData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val filterState = filterStateManager.state.asStateFlow()
    val statisticsState = statisticsStateManager.state.asStateFlow()

    private suspend fun initData() {
        logDebug("initData")
        filterStateManager.init(
            calendarRepository.findAllCalendars()
        )
        _initialized.update { true }

        onAction(StatisticsAction.SelectTimeRangeType(filterState.value.timeRangeType))
    }

    fun onAction(action: StatisticsAction) = when (action) {
        is StatisticsAction.SelectCalendar -> {
            filterStateManager.selectCalendar(action.calendar)

            val now = ZonedDateTime.now()
            val beginningOfDay = now.truncatedTo(ChronoUnit.DAYS).minusDays(1L)

            val queryTimeRange = ZonedDateTimeRange(beginningOfDay, now)
            viewModelScope.launch {
                val events = calendarRepository.findEventsInTimeRange(action.calendar, queryTimeRange)
                statisticsStateManager.recalculate(queryTimeRange, events)
            }
        }

        is StatisticsAction.SelectTimeRangeType -> {
            filterStateManager.selectTimeRangeType(action.type)

            viewModelScope.launch {
                filterState.collect {
                    val now = ZonedDateTime.now()
                    val from = when(it.timeRangeType) {
                        FilterTimeRangeType.DAY -> now.truncatedTo(ChronoUnit.DAYS)
                        FilterTimeRangeType.WEEK -> now.with(WeekFields.ISO.firstDayOfWeek).truncatedTo(ChronoUnit.DAYS)
                        FilterTimeRangeType.MONTH -> now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)
                    }

                    val queryTimeRange = ZonedDateTimeRange(from, now)

                    val events = calendarRepository.findEventsInTimeRange(it.selectedCalendar, queryTimeRange)
                    statisticsStateManager.recalculate(queryTimeRange, events)
                }
            }
        }
    }

}
