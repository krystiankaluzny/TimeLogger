package org.obywatelgcc.timelogger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.model.calendar.Calendar
import org.obywatelgcc.timelogger.model.calendar.CalendarEvent
import org.obywatelgcc.timelogger.model.calendar.CalendarEventColor
import org.obywatelgcc.timelogger.model.calendar.CalendarRepository
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class TimeEventViewModel(
    val calendarRepository: CalendarRepository
) : ViewModel() {

    private val tickerDelayMs = 1000L

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized
        .onStart { initCalendars() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _appName = MutableStateFlow("")
    val appName = _appName.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.READY_TO_START)
    val timerState = _timerState.asStateFlow()

    private val timeCalendarEventState = TimeCalendarEventState()
    val calendarState = timeCalendarEventState.state.asStateFlow()
    val availableCalendars = timeCalendarEventState.availableCalendars.asStateFlow()
    val selectedCalendar = timeCalendarEventState.selectedCalendar.asStateFlow()
    val availableColors = timeCalendarEventState.availableColors.asStateFlow()
    val selectedColor = timeCalendarEventState.selectedColor.asStateFlow()

    val eventTitle = timeCalendarEventState.eventTitle.asStateFlow()

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

    private val _calendarEventToSave = MutableSharedFlow<CalendarEvent>()
    val calendarEventToSave = _calendarEventToSave.asSharedFlow()

    private val _messageChannel = Channel<String>()
    val messageChannelFlow = _messageChannel.receiveAsFlow()

    private fun initCalendars() {
        viewModelScope.launch {
            timeCalendarEventState.init(
                calendarRepository.findAllCalendars(),
                calendarRepository.findAllEventColors()
            )

            _initialized.value = true
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

                    val event = CalendarEvent.of(
                        eventTitle.value,
                        startDateTime.value,
                        endDateTime.value,
                        selectedColor.value
                    )

                    val result = calendarRepository.addEventToCalendar(calendar, event)

                    handleSaveResult(result)
                } else {
                    handleValidationResult(validationResult)
                }
            }
        }
    }

    private fun checkData(): ValidationResult {
        if (eventTitle.value.isEmpty()) {
            return ValidationResult.EMPTY_TITLE
        }
        return ValidationResult.OK
    }

    private suspend fun handleValidationResult(validationResult: ValidationResult) {
        when (validationResult) {
            ValidationResult.OK -> TODO()
            ValidationResult.EMPTY_TITLE -> _messageChannel.send("Empty title")
            ValidationResult.DURATION_TOO_SHORT -> _messageChannel.send("Duration too short")
            ValidationResult.END_BEFORE_START -> _messageChannel.send("End before start")
        }
    }

    private suspend fun handleSaveResult(result: CalendarRepository.AddEventResult) {
        when (result.status) {
            CalendarRepository.AddEventResult.Status.ALREADY_EXISTS -> _messageChannel.send("Already exists")
            CalendarRepository.AddEventResult.Status.CREATED -> {
                reset()
                _messageChannel.send("Successfully saved")
            }

            CalendarRepository.AddEventResult.Status.ERROR -> _messageChannel.send("Saving failed")
        }
    }


    fun selectCalendar(calendar: Calendar) = timeCalendarEventState.select(calendar)
    fun selectColor(color: CalendarEventColor) = timeCalendarEventState.selectColor(color)
    fun updateEventTitle(title: String) = timeCalendarEventState.updateTitle(title)

    fun updateStartDate(localDate: LocalDate) = timeRangeState.updateStartDate(localDate)
    fun updateStartTime(localTime: LocalTime) = timeRangeState.updateStartTime(localTime)
    fun updateEndDate(localDate: LocalDate) = timeRangeState.updateEndDate(localDate)
    fun updateEndTime(localTime: LocalTime) = timeRangeState.updateEndTime(localTime)
}

enum class TimerState {
    READY_TO_START, STARTED, STOPPED
}

enum class ValidationResult {
    OK, EMPTY_TITLE, DURATION_TOO_SHORT, END_BEFORE_START
}