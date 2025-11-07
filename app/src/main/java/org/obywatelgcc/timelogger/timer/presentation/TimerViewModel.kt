package org.obywatelgcc.timelogger.timer.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.timer.model.Calendar
import org.obywatelgcc.timelogger.timer.model.CalendarEvent
import org.obywatelgcc.timelogger.timer.model.CalendarRepository
import org.obywatelgcc.timelogger.timer.presentation.calendar.CalendarState
import org.obywatelgcc.timelogger.timer.presentation.calendar.CalendarStateManager
import org.obywatelgcc.timelogger.timer.presentation.settings.SettingsState
import org.obywatelgcc.timelogger.timer.presentation.settings.SettingsStateManager
import org.obywatelgcc.timelogger.timer.presentation.timer.TimerState
import org.obywatelgcc.timelogger.timer.presentation.timer.TimerStateManager
import org.obywatelgcc.timelogger.timer.presentation.title.TitleState
import org.obywatelgcc.timelogger.timer.presentation.title.TitleStateManager

class TimerViewModel(
    savedStateHandle: SavedStateHandle,
    dataSoreManager: DataStoreManager,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val calendarStateManager =
        CalendarStateManager(viewModelScope, savedStateHandle, dataSoreManager, CalendarState())
    private val timerStateManager =
        TimerStateManager(viewModelScope, savedStateHandle, dataSoreManager, TimerState())
    private val titleStateManager =
        TitleStateManager(viewModelScope, savedStateHandle, dataSoreManager, TitleState(), calendarRepository)
    private val settingsStateManager =
        SettingsStateManager(viewModelScope, savedStateHandle, dataSoreManager, SettingsState())

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized
        .onStart { initData() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val calendarState = calendarStateManager.state.asStateFlow()
    val timerState = timerStateManager.state.asStateFlow()
    val titleState = titleStateManager.state.asStateFlow()
    val settingsState = settingsStateManager.state.asStateFlow()

    private val _effectsChannel = Channel<TimerEffect>()
    val effectsFlow = _effectsChannel.receiveAsFlow()

    fun onAction(action: TimerAction) =
        when (action) {
            is TimerAction.SelectCalendar -> calendarStateManager.selectCalendar(action.calendar)
            is TimerAction.SelectColor -> calendarStateManager.selectColor(action.color)
            is TimerAction.UpdateTitle -> titleStateManager.updateTitle(
                action.title,
                calendarState.value.selectedCalendar
            )

            is TimerAction.SelectSuggestion -> {
                titleStateManager.selectSuggestion(action.suggestion)
                calendarStateManager.selectSuggestedColor(action.suggestion.color)
            }

            TimerAction.StartTimer -> timerStateManager.start()
            TimerAction.StopTimer -> timerStateManager.stop()
            TimerAction.ResumeTimer -> timerStateManager.resume()
            TimerAction.RestartTimer -> timerStateManager.reset()
            is TimerAction.UpdateStartDate -> timerStateManager.updateStartDate(action.date)
            is TimerAction.UpdateStartTime -> timerStateManager.updateStartTime(action.time)
            is TimerAction.UpdateEndDate -> timerStateManager.updateEndDate(action.date)
            is TimerAction.UpdateEndTime -> timerStateManager.updateEndTime(action.time)
            is TimerAction.UpdateSavingType -> settingsStateManager.updateSavingType(action.newSavingType)
            TimerAction.TrySave -> trySave()
        }

    private suspend fun initData() {
        calendarStateManager.init(
            calendarRepository.findAllCalendars(),
            calendarRepository.findAllEventColors()
        )
        timerStateManager.init()
        titleStateManager.init()
        settingsStateManager.init()
        _initialized.update { true }
    }


    private fun trySave() {
        val selectedCalendar = calendarState.value.selectedCalendar
        var validationResult = ValidationResult.OK

        if (selectedCalendar == Calendar.Empty) {
            validationResult = ValidationResult.INVALID_CALENDAR
        } else {
            viewModelScope.launch {
                timerStateManager.stop()
                validationResult = checkData()

                if (validationResult == ValidationResult.OK) {

                    val localCalendarState = calendarState.value
                    val localTimerState = timerState.value
                    val localTitleState = titleState.value

                    val event = CalendarEvent.of(
                        localTitleState.eventTitleTrim,
                        localTimerState.startDateTime,
                        localTimerState.endDateTime,
                        localCalendarState.selectedColor
                    )

                    val result = calendarRepository.addEventToCalendar(selectedCalendar, event)

                    handleSaveResult(result)
                } else {
                    handleValidationResult(validationResult)
                }
            }
        }
    }

    private fun checkData(): ValidationResult {
        if (titleState.value.eventTitle.isBlank()) {
            return ValidationResult.EMPTY_TITLE
        }
        return ValidationResult.OK
    }

    private suspend fun handleSaveResult(result: CalendarRepository.AddEventResult) {
        when (result.status) {
            CalendarRepository.AddEventResult.Status.ALREADY_EXISTS -> _effectsChannel.send(TimerEffect.SavingMessage("Already exists"))
            CalendarRepository.AddEventResult.Status.CREATED -> {
                when (settingsState.value.savingType) {
                    SettingsState.SavingType.SaveOnly -> {
                        timerStateManager.reset()
                    }

                    SettingsState.SavingType.SaveAndStartNew -> {
                        timerStateManager.reset()
                        titleStateManager.clearTitle()
                        timerStateManager.start()
                        calendarStateManager.shiftColor()
                    }
                }

                _effectsChannel.send(TimerEffect.SavingMessage("Successfully saved"))
            }

            CalendarRepository.AddEventResult.Status.ERROR -> _effectsChannel.send(TimerEffect.SavingMessage("Saving failed"))
        }
    }

    private suspend fun handleValidationResult(validationResult: ValidationResult) {
        when (validationResult) {
            ValidationResult.OK -> TODO()
            ValidationResult.EMPTY_TITLE -> _effectsChannel.send(TimerEffect.ValidationError("Empty title"))
            ValidationResult.DURATION_TOO_SHORT -> _effectsChannel.send(TimerEffect.ValidationError("Duration too short"))
            ValidationResult.END_BEFORE_START -> _effectsChannel.send(TimerEffect.ValidationError("End before start"))
            ValidationResult.INVALID_CALENDAR -> TODO()
        }
    }

}

enum class ValidationResult {
    OK, EMPTY_TITLE, DURATION_TOO_SHORT, END_BEFORE_START, INVALID_CALENDAR
}