package org.obywatelgcc.timelogger.settings.model

import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEventColor

data class CalendarSettings(
    val selectedCalendar: Calendar = Calendar.Empty,
    val availableCalendars: List<Calendar> = emptyList(),
    val availableColors: List<CalendarEventColor> = emptyList()
)
