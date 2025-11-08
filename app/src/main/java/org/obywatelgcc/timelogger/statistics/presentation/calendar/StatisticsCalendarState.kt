package org.obywatelgcc.timelogger.statistics.presentation.calendar

import org.obywatelgcc.timelogger.core.model.Calendar

data class StatisticsCalendarState(
    val availableCalendars: List<Calendar> = listOf<Calendar>(),
    val selectedCalendar: Calendar = Calendar.Empty
)