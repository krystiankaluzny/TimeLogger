package org.obywatelgcc.timelogger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _appName = MutableStateFlow("")
    val appName = _appName.asStateFlow()

    private val _started = MutableStateFlow(false)
    val started = _started.asStateFlow()

    private val timeCalendarState = TimeCalendarState()
    val calendarState = timeCalendarState.state.asStateFlow()
    val availableCalendars = timeCalendarState.availableCalendars.asStateFlow()
    val selectedCalendar = timeCalendarState.selectedCalendar.asStateFlow()

    private val _entryTitle = MutableStateFlow("")
    val entryTitle = _entryTitle.asStateFlow()

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

    private val _calendarEntryToSave = MutableSharedFlow<CalendarEntry>()
    val calendarEntryToSave = _calendarEntryToSave.asSharedFlow()

    private val _messageToShow = MutableSharedFlow<String>()
    val messageToShow = _messageToShow.asSharedFlow()

    init {
        initCalendars()
    }

    private fun initCalendars() {
        viewModelScope.launch {
            timeCalendarState.init(calendarRepository.findAll())
        }
    }

    fun setAppName(appName: String) {
        _appName.value = appName
    }

    fun reset() {
        timeRangeState.reset()
    }

    fun start() {
        if (!_started.value) {
            _started.value = true
            viewModelScope.launch {
                while (_started.value) {
                    timeRangeState.refreshEndDateTime()
                    delay(tickerDelayMs)
                }
            }
        }
    }

    fun stop() {
        if (_started.value) {
            _started.value = false
            timeRangeState.refreshEndDateTime()
        }
    }

    fun trySave() {
        selectedCalendar.value?.also { calendar ->
            viewModelScope.launch {
                stop()
                val entry = CalendarEntry.of(
                    entryTitle.value,
                    startDateTime.value,
                    endDateTime.value
                )
                val result = calendarRepository.addEntryToCalendar(calendar, entry)

                when(result.status) {
                    CalendarRepository.AddEntryResult.Status.ALREADY_EXISTS -> _messageToShow.emit("Already exists")
                    CalendarRepository.AddEntryResult.Status.CREATED -> _messageToShow.emit("Successfully saved")
                    CalendarRepository.AddEntryResult.Status.ERROR -> _messageToShow.emit("Saving failed")
                }
                //NOTE: replace calendarRepository.addEntryToCalendar with this emit,
                // to call another activity that handle a new entry
//                _calendarEntryToSave.emit(entry)
            }
        }
    }

    fun selectCalendar(calendar: Calendar) = timeCalendarState.select(calendar)
    fun updateEntryTitle(description: String) {
        _entryTitle.value = description
    }

    fun updateStartDate(localDate: LocalDate) = timeRangeState.updateStartDate(localDate)
    fun updateStartTime(localTime: LocalTime) = timeRangeState.updateStartTime(localTime)
    fun updateEndDate(localDate: LocalDate) = timeRangeState.updateEndDate(localDate)
    fun updateEndTime(localTime: LocalTime) = timeRangeState.updateEndTime(localTime)
}

