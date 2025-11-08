package org.obywatelgcc.timelogger.core.model

import kotlinx.coroutines.delay

class TestCalendarRepositoryImpl : CalendarRepository {
    private val calendars = listOf(
        Calendar(1, "abc@gmail.pl", "google.com", "abc@gmail.pl", "abc@gmail.pl"),
        Calendar(2, "abc@gmail.pl", "test.google.com", "Test calendar", "abc@gmail.pl"),
        Calendar(3, "Local calendar", "local", "Local calendar", "abc@gmail.pl"),
    )

    private val calendarEventColors = listOf(
        CalendarEventColor(
            "1",
            "#ff445f59",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),
        CalendarEventColor(
            "2",
            "#ffaacaa3",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),
        CalendarEventColor(
            "3",
            "#ff660d11",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),
        CalendarEventColor(
            "4",
            "#ff4b3294",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),
        CalendarEventColor(
            "5",
            "#ffd67992",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),
        CalendarEventColor(
            "6",
            "#ff157ca8",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),
        CalendarEventColor(
            "7",
            "#ffb498d0",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),
        CalendarEventColor(
            "8",
            "#fffefd4c",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),
        CalendarEventColor(
            "9",
            "#ff5a3f76",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),
        CalendarEventColor(
            "10",
            "#fffd08ff",
            "",
            calendars[0].accountName,
            calendars[0].accountType
        ),

        CalendarEventColor(
            "1",
            "#ff445f59",
            "",
            calendars[1].accountName,
            calendars[1].accountType
        ),
        CalendarEventColor(
            "2",
            "#ffaacaa3",
            "",
            calendars[1].accountName,
            calendars[1].accountType
        ),
        CalendarEventColor(
            "3",
            "#ff660d11",
            "",
            calendars[1].accountName,
            calendars[1].accountType
        ),

        CalendarEventColor(
            "1",
            "#ff445f59",
            "",
            calendars[2].accountName,
            calendars[2].accountType
        ),
        CalendarEventColor(
            "2",
            "#ffaacaa3",
            "",
            calendars[2].accountName,
            calendars[2].accountType
        ),
        CalendarEventColor(
            "3",
            "#ff660d11",
            "",
            calendars[2].accountName,
            calendars[2].accountType
        ),
        CalendarEventColor(
            "4",
            "#ff4b3294",
            "",
            calendars[2].accountName,
            calendars[2].accountType
        ),
        CalendarEventColor(
            "5",
            "#ffd67992",
            "",
            calendars[2].accountName,
            calendars[2].accountType
        ),
        CalendarEventColor(
            "6",
            "#ff157ca8",
            "",
            calendars[2].accountName,
            calendars[2].accountType
        ),
        CalendarEventColor(
            "7",
            "#ffb498d0",
            "",
            calendars[2].accountName,
            calendars[2].accountType
        ),

        CalendarEventColor("101", "#ffeade69", "", "otherAccountName", "otherAccountType"),
        CalendarEventColor("102", "#ffb7ef2d", "", "otherAccountName", "otherAccountType"),
        CalendarEventColor("103", "#ff066b3b", "", "otherAccountName", "otherAccountType"),
    )

    private val calendarEvents = mutableMapOf<Calendar, MutableList<CalendarEvent>>()

    override suspend fun findAllCalendars(): List<Calendar> {
        delay(1_000)
        return calendars
    }

    override suspend fun findAllEventColors(): List<CalendarEventColor> {
        delay(1_000)
        calendarEventColors.forEach {
            println("$it - ${it.colorAsLong()}")
        }
        return calendarEventColors
    }

    override suspend fun addEventToCalendar(
        calendar: Calendar,
        event: CalendarEvent
    ): CalendarRepository.AddEventResult {
        delay(2_000)
        calendarEvents.computeIfAbsent(calendar) { c -> mutableListOf() }
            .add(event)

        return CalendarRepository.AddEventResult(CalendarRepository.AddEventResult.Status.CREATED, event)
    }

    override suspend fun findEventsContainsTitle(
        calendar: Calendar,
        title: String
    ): List<CalendarEvent> {
        return calendarEvents.computeIfAbsent(calendar) { c -> mutableListOf() }
            .filter { it.title.contains(title, true) }
    }
}