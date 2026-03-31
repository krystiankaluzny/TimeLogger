package org.obywatelgcc.timelogger.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.settings.model.SettingsHolder
import org.obywatelgcc.timelogger.settings.presentation.SettingsStateManager

class SettingsViewModel(
    savedStateHandle: SavedStateHandle,
    dataSoreManager: DataStoreManager,
    settingsHolder: SettingsHolder
) : ViewModel() {

    private val settingsStateManager =
        SettingsStateManager(viewModelScope, savedStateHandle, dataSoreManager, settingsHolder)

    private val _initialized = MutableStateFlow(false)
    val initialized = _initialized.asStateFlow()

    val sleepWindowSettings = settingsHolder.sleepWindowsSettings

    init {
        viewModelScope.launch {
            initData()
        }
    }

    private suspend fun initData() {
        settingsStateManager.init()
        _initialized.update { true }
    }

    fun onAction(action: SettingsAction) = when (action) {
        is SettingsAction.SleepWindowEnabled -> settingsStateManager.setSleepWindowEnabled(action.enabled)
        is SettingsAction.SleepWindowStart -> settingsStateManager.setSleepWindowStart(action.time)
        is SettingsAction.SleepWindowEnd -> settingsStateManager.setSleepWindowEnd(action.time)
    }
}
