package org.obywatelgcc.timelogger.settings.model

import kotlinx.coroutines.flow.StateFlow

interface SettingsProvider {
    val sleepWindowsSettings: StateFlow<SleepWindowSettings>
    val otherSettings: StateFlow<OtherSettings>
    val calendarSettings: StateFlow<CalendarSettings>
}