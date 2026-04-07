package org.obywatelgcc.timelogger.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.CalendarRepository
import org.obywatelgcc.timelogger.settings.model.SettingsHolder
import org.obywatelgcc.timelogger.settings.presentation.SettingsStateManager

class SettingsViewModel(
    savedStateHandle: SavedStateHandle,
    dataSoreManager: DataStoreManager,
    private val settingsHolder: SettingsHolder,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val settingsStateManager =
        SettingsStateManager(viewModelScope, savedStateHandle, dataSoreManager, settingsHolder)

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized.asStateFlow()

    val calendarSettings = settingsHolder.calendarSettings
    val availableCalendars = settingsHolder.availableCalendarSettings
    val sleepWindowSettings = settingsHolder.sleepWindowsSettings
    val otherSettings = settingsHolder.otherSettings

    init {
        viewModelScope.launch {
            initData()
        }
    }

    private suspend fun initData() {
        settingsStateManager.init(
            calendarRepository.findAllCalendars(),
            calendarRepository.findAllEventColors()
        )
        _initialized.update { true }
    }

    fun onAction(action: SettingsAction) = when (action) {
        is SettingsAction.SelectCalendar -> settingsStateManager.selectCalendar(action.calendar)
        is SettingsAction.SleepWindowEnabled -> settingsStateManager.setSleepWindowEnabled(action.enabled)
        is SettingsAction.SleepWindowStart -> settingsStateManager.setSleepWindowStart(action.time)
        is SettingsAction.SleepWindowEnd -> settingsStateManager.setSleepWindowEnd(action.time)
        is SettingsAction.CountUnmeasuredGapsEnabled -> settingsStateManager.setCountUnmeasuredGaps(action.enabled)
    }
}
