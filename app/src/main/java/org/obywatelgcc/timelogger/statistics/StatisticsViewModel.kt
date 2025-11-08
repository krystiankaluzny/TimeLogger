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
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.CalendarRepository
import org.obywatelgcc.timelogger.statistics.presentation.calendar.StatisticsCalendarState
import org.obywatelgcc.timelogger.statistics.presentation.calendar.StatisticsCalendarStateManager
import org.obywatelgcc.timelogger.utils.logDebug

class StatisticsViewModel(
    savedStateHandle: SavedStateHandle,
    dataSoreManager: DataStoreManager,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val statisticsCalendarStateManager =
        StatisticsCalendarStateManager(viewModelScope, savedStateHandle, dataSoreManager, StatisticsCalendarState())

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized
        .onStart { initData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val statisticsCalendarState = statisticsCalendarStateManager.state.asStateFlow()

    private suspend fun initData() {
        logDebug("initData")
        statisticsCalendarStateManager.init(
            calendarRepository.findAllCalendars()
        )
        _initialized.update { true }
    }

}
