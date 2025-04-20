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

    private val _timerState = MutableStateFlow(TimerState.READY_TO_START)
    val timerState = _timerState.asStateFlow()

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
        _timerState.value = TimerState.READY_TO_START
        timeRangeState.reset()
    }

    fun start() {
        if (_timerState.value == TimerState.READY_TO_START) {
            startTimer()
        }
    }

    fun stop() {
        if (_timerState.value == TimerState.STARTED) {
            _timerState.value = TimerState.STOPPED
            timeRangeState.refreshEndDateTime()
        }
    }

    fun resume() {
        if (_timerState.value == TimerState.STOPPED) {
            startTimer()
        }
    }

    private fun startTimer() {
        _timerState.value = TimerState.STARTED
        viewModelScope.launch {
            while (_timerState.value == TimerState.STARTED) {
                timeRangeState.refreshEndDateTime()
                delay(tickerDelayMs)
            }
        }
    }

    fun trySave() {
        selectedCalendar.value?.also { calendar ->
            viewModelScope.launch {
                stop()
                val validationResult = checkData()

                if (validationResult == ValidationResult.OK) {
                    val entry = CalendarEntry.of(
                        entryTitle.value,
                        startDateTime.value,
                        endDateTime.value
                    )
                    val result = calendarRepository.addEntryToCalendar(calendar, entry)

                    handleSaveResult(result)
                } else {
                    handleValidationResult(validationResult)
                }
            }
        }
    }

    private fun checkData(): ValidationResult {
        if(entryTitle.value.isEmpty()) {
            return ValidationResult.EMPTY_TITLE
        }
        return ValidationResult.OK
    }

    private suspend fun handleValidationResult(validationResult: ValidationResult) {
        when(validationResult) {
            ValidationResult.OK -> TODO()
            ValidationResult.EMPTY_TITLE -> _messageToShow.emit("Empty title")
            ValidationResult.DURATION_TOO_SHORT -> TODO()
        }
    }

    private suspend fun handleSaveResult(result: CalendarRepository.AddEntryResult) {
        when (result.status) {
            CalendarRepository.AddEntryResult.Status.ALREADY_EXISTS -> _messageToShow.emit("Already exists")
            CalendarRepository.AddEntryResult.Status.CREATED -> {
                reset()
                _messageToShow.emit("Successfully saved")
            }

            CalendarRepository.AddEntryResult.Status.ERROR -> _messageToShow.emit("Saving failed")
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

enum class TimerState {
    READY_TO_START, STARTED, STOPPED
}

enum class ValidationResult {
    OK, EMPTY_TITLE, DURATION_TOO_SHORT
}