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
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeAction
import org.obywatelgcc.timelogger.core.presentation.filter.FilterTimeRangeState
import org.obywatelgcc.timelogger.core.presentation.filter.FilterTimeRangeStateManager
import org.obywatelgcc.timelogger.settings.model.SettingsProvider
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsState
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsStateManager
import org.obywatelgcc.timelogger.utils.logDebug

class StatisticsViewModel(
    savedStateHandle: SavedStateHandle,
    dataSoreManager: DataStoreManager,
    private val settingsProvider: SettingsProvider,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val filterTimeRangeStateManager =
        FilterTimeRangeStateManager(viewModelScope, savedStateHandle, dataSoreManager, FilterTimeRangeState(), "statsFilterPref")
    private val statisticsStateManager =
        StatisticsStateManager(viewModelScope, savedStateHandle, dataSoreManager, StatisticsState())

    private val calendarEventCache: Cache<Pair<Calendar, ZonedDateTimeRange>, List<CalendarEvent>> =
        TimedCache.expiringEveryMinutes(5L)

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized
        .onStart { initData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val filterState = filterTimeRangeStateManager.state.asStateFlow()
    val statisticsState = statisticsStateManager.state.asStateFlow()

    private suspend fun initData() {
        logDebug("initData")
        filterTimeRangeStateManager.init()
        viewModelScope.launch {
            settingsProvider.calendarSettings.collect {
                refreshStatisticsData()
            }
        }
        viewModelScope.launch {
            settingsProvider.sleepWindowsSettings.collect {
                refreshStatisticsData()
            }
        }
        viewModelScope.launch {
            settingsProvider.otherSettings.collect {
                refreshStatisticsData()
            }
        }
        launchRefreshStatisticsData()
        _initialized.update { true }
    }

    fun onFilterAction(action: FilterTimeRangeAction) = when (action) {
        is FilterTimeRangeAction.SelectTimeRangeType -> {
            filterTimeRangeStateManager.selectTimeRangeType(action.type)
            launchRefreshStatisticsData()
        }

        FilterTimeRangeAction.PreviousRange -> {
            filterTimeRangeStateManager.calculatePreviousRange()
            launchRefreshStatisticsData()
        }

        FilterTimeRangeAction.NextRange -> {
            filterTimeRangeStateManager.calculateNextRange()
            launchRefreshStatisticsData()
        }

        FilterTimeRangeAction.ResetRange -> {
            filterTimeRangeStateManager.resetRange()
            launchRefreshStatisticsData()
        }
    }

    private fun launchRefreshStatisticsData() {
        viewModelScope.launch {
            refreshStatisticsData()
        }
    }

    private suspend fun refreshStatisticsData() {
        val selectedCalendar = settingsProvider.calendarSettings.value.selectedCalendar
        val queryTimeRange = filterState.value.timeRange
        val key = selectedCalendar to queryTimeRange
        val events = calendarEventCache.get(key)
            ?: calendarRepository.findEventsInTimeRange(selectedCalendar, queryTimeRange)
                .also { fetchedEvents ->
                    calendarEventCache.put(key, fetchedEvents)
                }

        statisticsStateManager.recalculate(
            queryTimeRange,
            events,
            settingsProvider.sleepWindowsSettings.value,
            settingsProvider.otherSettings.value
        )
    }
}
