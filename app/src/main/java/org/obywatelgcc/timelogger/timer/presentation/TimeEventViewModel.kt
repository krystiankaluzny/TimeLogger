package org.obywatelgcc.timelogger.timer.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
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
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.timer.model.calendar.Calendar
import org.obywatelgcc.timelogger.timer.model.calendar.CalendarEvent
import org.obywatelgcc.timelogger.timer.model.calendar.CalendarEventColor
import org.obywatelgcc.timelogger.timer.model.calendar.CalendarRepository
import org.obywatelgcc.timelogger.utils.logInfo
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class TimeEventViewModel(
    savedStateHandle: SavedStateHandle,
    private val dataSoreManager: DataStoreManager,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized
        .onStart { initData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _appName = MutableStateFlow("")
    val appName = _appName.asStateFlow()


    private val timeCalendarEventState =
        TimeCalendarEventState(viewModelScope, savedStateHandle, dataSoreManager)
    val calendarState = timeCalendarEventState.state.asStateFlow()
    val availableCalendars = timeCalendarEventState.availableCalendars.asStateFlow()
    val selectedCalendar = timeCalendarEventState.selectedCalendar.asStateFlow()
    val availableColors = timeCalendarEventState.availableColors.asStateFlow()
    val selectedColor = timeCalendarEventState.selectedColor.asStateFlow()


    private val timeEventState = TimerEventState(viewModelScope, savedStateHandle, dataSoreManager)

    val timerState = timeEventState.timerState.asStateFlow()
    val eventTitle = timeEventState.eventTitle.asStateFlow()
    val startDateTime = timeEventState.startDateTime.asStateFlow()
    val endDateTime = timeEventState.endDateTime.asStateFlow()

    val durationStr = timeEventState.duration.map {
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

    private suspend fun initData() {
        logInfo("initData")

        initCalendars()
        timeEventState.init()

        _initialized.value = true
    }

    private suspend fun initCalendars() {
        timeCalendarEventState.init(
            calendarRepository.findAllCalendars(),
            calendarRepository.findAllEventColors()
        )
    }

    fun setAppName(appName: String) {
        _appName.value = appName
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

    fun updateEventTitle(title: String) = timeEventState.updateTitle(title)
    fun updateStartDate(localDate: LocalDate) = timeEventState.updateStartDate(localDate)
    fun updateStartTime(localTime: LocalTime) = timeEventState.updateStartTime(localTime)
    fun updateEndDate(localDate: LocalDate) = timeEventState.updateEndDate(localDate)
    fun updateEndTime(localTime: LocalTime) = timeEventState.updateEndTime(localTime)

    fun reset() = timeEventState.reset()
    fun start() = timeEventState.start()
    fun stop() = timeEventState.stop()
    fun resume() = timeEventState.resume()
}

enum class ValidationResult {
    OK, EMPTY_TITLE, DURATION_TOO_SHORT, END_BEFORE_START
}
