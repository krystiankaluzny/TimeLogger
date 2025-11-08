package org.obywatelgcc.timelogger.core.model


interface CalendarRepository {

    suspend fun findAllCalendars(): List<Calendar>
    suspend fun findAllEventColors(): List<CalendarEventColor>
    suspend fun addEventToCalendar(calendar: Calendar, event: CalendarEvent): AddEventResult
    suspend fun findEventsContainsTitle(calendar: Calendar, title: String): List<CalendarEvent>

    data class AddEventResult(
        val status: Status,
        val entry: CalendarEvent
    ) {
        enum class Status { ALREADY_EXISTS, CREATED, ERROR }
    }
}
