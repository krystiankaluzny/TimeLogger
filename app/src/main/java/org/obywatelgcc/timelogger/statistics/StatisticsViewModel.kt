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
import org.obywatelgcc.timelogger.core.cache.Cache
import org.obywatelgcc.timelogger.core.cache.TimedCache
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEvent
import org.obywatelgcc.timelogger.core.model.CalendarRepository
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.settings.model.SettingsProvider
import org.obywatelgcc.timelogger.statistics.presentation.filter.FilterState
import org.obywatelgcc.timelogger.statistics.presentation.filter.FilterStateManager
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsState
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsStateManager
import org.obywatelgcc.timelogger.utils.logDebug

class StatisticsViewModel(
    savedStateHandle: SavedStateHandle,
    dataSoreManager: DataStoreManager,
    private val settingsProvider: SettingsProvider,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val filterStateManager =
        FilterStateManager(viewModelScope, savedStateHandle, dataSoreManager, FilterState())
    private val statisticsStateManager =
        StatisticsStateManager(viewModelScope, savedStateHandle, dataSoreManager, StatisticsState())

    private val calendarEventCache: Cache<Pair<Calendar, ZonedDateTimeRange>, List<CalendarEvent>> =
        TimedCache.expiringEveryMinutes(5L)

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
        viewModelScope.launch {
            settingsProvider.sleepWindowsSettings.collect {
                refreshStatisticsData()
            }
        }
        launchRefreshStatisticsData()
        _initialized.update { true }
    }

    fun onAction(action: StatisticsAction) = when (action) {
        is StatisticsAction.SelectCalendar -> {
            filterStateManager.selectCalendar(action.calendar)
            launchRefreshStatisticsData()
        }

        is StatisticsAction.SelectTimeRangeType -> {
            filterStateManager.selectTimeRangeType(action.type)
            launchRefreshStatisticsData()
        }

        StatisticsAction.PreviousRange -> {
            filterStateManager.calculatePreviousRange()
            launchRefreshStatisticsData()
        }

        StatisticsAction.NextRange -> {
            filterStateManager.calculateNextRange()
            launchRefreshStatisticsData()
        }

        StatisticsAction.ResetRange -> {
            filterStateManager.resetRange()
            launchRefreshStatisticsData()
        }
    }

    private fun launchRefreshStatisticsData() {
        viewModelScope.launch {
            refreshStatisticsData()
        }
    }

    private suspend fun refreshStatisticsData() {
        val fs = filterState.value
        val selectedCalendar = fs.selectedCalendar
        val queryTimeRange = fs.timeRange
        val key = selectedCalendar to queryTimeRange
        val events = calendarEventCache.get(key)
            ?: calendarRepository.findEventsInTimeRange(selectedCalendar, queryTimeRange)
                .also { fetchedEvents ->
                    calendarEventCache.put(key, fetchedEvents)
                }

        statisticsStateManager.recalculate(
            queryTimeRange,
            events,
            settingsProvider.sleepWindowsSettings.value
        )
    }
}
