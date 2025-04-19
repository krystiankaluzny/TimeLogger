package org.obywatelgcc.timelogger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.model.calendar.Calendar
import org.obywatelgcc.timelogger.model.calendar.CalendarEntry
import org.obywatelgcc.timelogger.model.calendar.CalendarRepository
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class TimeEntryViewModel(
    val calendarRepository: CalendarRepository
) : ViewModel() {

    private val tickerDelayMs = 1000L

    private val _uiState = MutableStateFlow(UiState.INITIALIZING)
    val uiState = _uiState.asStateFlow()

    private val _started = MutableStateFlow(false)
    val started = _started.asStateFlow()

    private val timeCalendarState = TimeCalendarState()
    val availableCalendars = timeCalendarState.availableCalendars.asStateFlow()
    val selectedCalendar = timeCalendarState.selectedCalendar.asStateFlow()

    private val _taskDescription = MutableStateFlow("")
    val taskDescription = _taskDescription.asStateFlow()

    private val timeRangeState = TimeRangeState(viewModelScope)
    val startDateTime = timeRangeState.startDateTime.asStateFlow()
    val endDateTime = timeRangeState.endDateTime.asStateFlow()

    val durationStr = timeRangeState.duration.map {
        val durationSeconds = it.seconds
        val hoursPart = durationSeconds / 3600
        val minutesPart = (durationSeconds / 60) % 60
        val secondsPart = durationSeconds % 60

        String.format(
            Locale.getDefault(Locale.Category.FORMAT),
            "%d:%02d:%02d",
            hoursPart,
            minutesPart,
            secondsPart
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    init {
        initCalendars()
    }

    private fun initCalendars() {
        viewModelScope.launch {
            _uiState.value = UiState.INITIALIZING
            timeCalendarState.init(calendarRepository.findAll())
            _uiState.value = UiState.INITIALIZED
        }
    }

    fun reset() {
        timeRangeState.reset()
    }

    fun start() {
        _started.value = true

        viewModelScope.launch {
            while (_started.value) {
                timeRangeState.refreshEndDateTime()
                delay(tickerDelayMs)
            }
        }
    }

    fun stop() {
        if (_started.value) {
            _started.value = false
            timeRangeState.refreshEndDateTime()
        }
    }

    fun save() {
        selectedCalendar.value?.also { calendar ->
            viewModelScope.launch {
                stop()
                val entry = CalendarEntry(
                    taskDescription.value,
                    startDateTime.value,
                    endDateTime.value
                )
                calendarRepository.addEntryToCalendar(calendar, entry)
            }
        }
    }

    fun selectCalendar(calendar: Calendar) = timeCalendarState.select(calendar)
    fun updateTaskDescription(description: String) {
        _taskDescription.value = description
    }

    fun updateStartDate(localDate: LocalDate) = timeRangeState.updateStartDate(localDate)
    fun updateStartTime(localTime: LocalTime) = timeRangeState.updateStartTime(localTime)
    fun updateEndDate(localDate: LocalDate) = timeRangeState.updateEndDate(localDate)
    fun updateEndTime(localTime: LocalTime) = timeRangeState.updateEndTime(localTime)
}

enum class UiState {
    INITIALIZING, INITIALIZED
}