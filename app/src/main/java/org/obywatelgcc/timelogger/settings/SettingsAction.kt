package org.obywatelgcc.timelogger.settings

import java.time.LocalTime

sealed class SettingsAction {
    data class SleepWindowEnabled(val enabled: Boolean) : SettingsAction()
    data class SleepWindowStart(val time: LocalTime) : SettingsAction()
    data class SleepWindowEnd(val time: LocalTime) : SettingsAction()
    data class CountUnmeasuredGapsEnabled(val enabled: Boolean) : SettingsAction()
}
