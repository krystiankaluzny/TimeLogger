package org.obywatelgcc.timelogger.settings

import org.obywatelgcc.timelogger.core.model.Calendar
import java.time.LocalTime

sealed class SettingsAction {
    data class SelectCalendar(val calendar: Calendar) : SettingsAction()
    data class SleepWindowEnabled(val enabled: Boolean) : SettingsAction()
    data class SleepWindowStart(val time: LocalTime) : SettingsAction()
    data class SleepWindowEnd(val time: LocalTime) : SettingsAction()
    data class CountUnmeasuredGapsEnabled(val enabled: Boolean) : SettingsAction()
}
