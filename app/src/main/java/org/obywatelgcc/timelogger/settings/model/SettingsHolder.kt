package org.obywatelgcc.timelogger.settings.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsHolder(): SettingsProvider {
    private val _sleepWindowsSettings = MutableStateFlow<SleepWindowSettings>(SleepWindowSettings())
    override val sleepWindowsSettings: StateFlow<SleepWindowSettings> = _sleepWindowsSettings.asStateFlow()

    private val _otherSettings = MutableStateFlow<OtherSettings>(OtherSettings())
    override val otherSettings: StateFlow<OtherSettings> = _otherSettings.asStateFlow()

    fun updateSleepWindow(function: (SleepWindowSettings) -> SleepWindowSettings) {
        _sleepWindowsSettings.update(function)
    }

    fun updateOtherSettings(function: (OtherSettings) -> OtherSettings) {
        _otherSettings.update(function)
    }
}