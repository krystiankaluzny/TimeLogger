package org.obywatelgcc.timelogger.timer.presentation.title

import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEventColor

data class TitleState(
    val eventTitle: String = "",
    val suggestions: List<Suggestion> = listOf<Suggestion>(),
    val calendar: Calendar = Calendar.Empty,
    val availableColors: List<CalendarEventColor> = listOf(),
    val selectedColor: CalendarEventColor = CalendarEventColor.Empty
) {

    val eventTitleTrim: String by lazy { eventTitle.trim() }

    data class Suggestion(
        val value: String,
        val color: CalendarEventColor?,

        val prefix: String,
        val match: String,
        val suffix: String
    )
}
