package org.obywatelgcc.timelogger.timer.presentation.title

import org.obywatelgcc.timelogger.timer.model.CalendarEventColor

data class TitleState(
    val eventTitle: String = "",
    val suggestions: List<Suggestion> = listOf<Suggestion>()
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
