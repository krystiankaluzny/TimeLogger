package org.obywatelgcc.timelogger.settings.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsHolder(): SettingsProvider {
    private val _sleepWindowsSettings = MutableStateFlow<SleepWindowSettings>(SleepWindowSettings())
    override val sleepWindowsSettings: StateFlow<SleepWindowSettings> = _sleepWindowsSettings.asStateFlow()

    fun updateSleepWindow(function: (SleepWindowSettings) -> SleepWindowSettings) {
        _sleepWindowsSettings.update(function)
    }

}