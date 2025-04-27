package org.obywatelgcc.timelogger.timer.presentation.calendar

import org.obywatelgcc.timelogger.timer.model.Calendar
import org.obywatelgcc.timelogger.timer.model.CalendarEventColor

data class CalendarState(
    val availableCalendars: List<Calendar> = listOf<Calendar>(),
    val selectedCalendar: Calendar = Calendar.Empty,
    val availableColors: List<CalendarEventColor> = listOf<CalendarEventColor>(),
    val selectedColor: CalendarEventColor = CalendarEventColor.Empty
)