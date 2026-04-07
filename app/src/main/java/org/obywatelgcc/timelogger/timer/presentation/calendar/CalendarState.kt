package org.obywatelgcc.timelogger.timer.presentation.calendar

import org.obywatelgcc.timelogger.core.model.CalendarEventColor

data class CalendarState(
    val availableColors: List<CalendarEventColor> = listOf(),
    val selectedColor: CalendarEventColor = CalendarEventColor.Empty
)
