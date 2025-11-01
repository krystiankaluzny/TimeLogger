package org.obywatelgcc.timelogger.timer.model


interface CalendarRepository {

    suspend fun findAllCalendars(): List<Calendar>
    suspend fun findAllEventColors(): List<CalendarEventColor>
    suspend fun addEventToCalendar(calendar: Calendar, event: CalendarEvent): AddEventResult
    suspend fun findEventsContainsTitle(title: String): List<CalendarEvent>

    data class AddEventResult(
        val status: Status,
        val entry: CalendarEvent
    ) {
        enum class Status { ALREADY_EXISTS, CREATED, ERROR }
    }
}
