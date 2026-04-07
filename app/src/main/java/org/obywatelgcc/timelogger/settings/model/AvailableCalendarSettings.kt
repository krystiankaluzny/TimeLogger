package org.obywatelgcc.timelogger.settings.model

import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEventColor

data class AvailableCalendarSettings(
    val allCalendars: List<Calendar> = emptyList(),
    val allColors: List<CalendarEventColor> = emptyList()
)
