package org.obywatelgcc.timelogger.model.calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import kotlinx.coroutines.delay
import org.obywatelgcc.timelogger.model.calendar.CalendarRepository.AddEventResult
import org.obywatelgcc.timelogger.model.calendar.CalendarRepository.AddEventResult.Status
import java.time.ZonedDateTime


interface CalendarRepository {

    suspend fun findAllCalendars(): List<Calendar>
    suspend fun findAllEventColors(): List<CalendarEventColor>
    suspend fun addEventToCalendar(calendar: Calendar, event: CalendarEvent): AddEventResult

    data class AddEventResult(
        val status: Status,
        val entry: CalendarEvent
    ) {
        enum class Status { ALREADY_EXISTS, CREATED, ERROR }
    }
}

class CalendarRepositoryImpl(
    androidContext: Context
) : CalendarRepository {

    val contentResolver: ContentResolver = androidContext.contentResolver

    private var calenderEntryCached = mutableMapOf<EntryKey, CalendarEvent>()

    private data class EntryKey(val title: String, val start: ZonedDateTime)

    private data class Projection(val columns: List<String>) {
        val projection = columns.toTypedArray()
        val columnIndex = columns.withIndex().associateBy({ it.value }, { it.index })

        fun index(column: String): Int {
            return columnIndex[column]!!
        }
    }

    companion object {
        val CALENDAR_URI: Uri = CalendarContract.Calendars.CONTENT_URI
        val EVENT_URI: Uri = CalendarContract.Events.CONTENT_URI
        val COLOR_URI: Uri = CalendarContract.Colors.CONTENT_URI

        private val calendarsProjection: Projection = Projection(
            listOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.OWNER_ACCOUNT
            )
        )

        private val colorsProjection: Projection = Projection(
            listOf(
                CalendarContract.Colors.COLOR_KEY,
                CalendarContract.Colors.COLOR,
                CalendarContract.Colors.COLOR_TYPE,
                CalendarContract.Colors.ACCOUNT_NAME,
                CalendarContract.Colors.ACCOUNT_TYPE
            )
        )
    }

    override suspend fun findAllCalendars(): List<Calendar> {
        Log.d("CalendarRepository", "findAllCalendars")

        val result = mutableListOf<Calendar>()

        val calenderCursor: Cursor? =
            contentResolver.query(CALENDAR_URI, calendarsProjection.projection, null, null, null)
        calenderCursor?.apply {
            while (moveToNext()) {
                result.add(
                    Calendar(
                        id = getLong(calendarsProjection.index(CalendarContract.Calendars._ID)),
                        accountName = getString(calendarsProjection.index(CalendarContract.Calendars.ACCOUNT_NAME)),
                        accountType = getString(calendarsProjection.index(CalendarContract.Calendars.ACCOUNT_TYPE)),
                        displayName = getString(calendarsProjection.index(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)),
                        ownerName = getString(calendarsProjection.index(CalendarContract.Calendars.OWNER_ACCOUNT))
                    )
                )
            }
            close()
        }

        return result
    }

    override suspend fun findAllEventColors(): List<CalendarEventColor> {
        Log.d("CalendarRepository", "findAllEventColors")

        val result = mutableSetOf<CalendarEventColor>()

        val selection = "(${CalendarContract.Colors.COLOR_TYPE} = ?)"
        val selectionArgs = arrayOf(CalendarContract.Colors.TYPE_EVENT.toString())

        val colorCursor: Cursor? =
            contentResolver.query(
                COLOR_URI,
                colorsProjection.projection,
                selection,
                selectionArgs,
                null
            )
        colorCursor?.apply {
            while (moveToNext()) {
                result.add(
                    CalendarEventColor(
                        key = getString(colorsProjection.index(CalendarContract.Colors.COLOR_KEY)),
                        color = getString(colorsProjection.index(CalendarContract.Colors.COLOR)),
                        type = getString(colorsProjection.index(CalendarContract.Colors.COLOR_TYPE)),
                        accountName = getString(colorsProjection.index(CalendarContract.Colors.ACCOUNT_NAME)),
                        accountType = getString(colorsProjection.index(CalendarContract.Colors.ACCOUNT_TYPE)),
                    )
                )
            }
            close()
        }

        Log.d("CalendarRepository", "findAllEventColors: $result")

        return result.toList()
    }

    override suspend fun addEventToCalendar(
        calendar: Calendar,
        event: CalendarEvent
    ): AddEventResult {
        Log.d("CalendarRepository", "addEventToCalendar: $calendar, $event")

        val entryKey = EntryKey(event.title, event.start)
        val cachedEntry = calenderEntryCached[entryKey]
        if (cachedEntry != null) {
            return AddEventResult(Status.ALREADY_EXISTS, event)
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, event.startMillis())
            put(CalendarContract.Events.DTEND, event.endMillis())
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, "")
            put(CalendarContract.Events.CALENDAR_ID, calendar.id)
            put(CalendarContract.Events.EVENT_TIMEZONE, event.zoneIdName())
            put(CalendarContract.Events.EVENT_COLOR_KEY, event.color?.key ?: "")
        }

        Log.d("CalendarRepository", "addEntryToCalendar: $values")

        val savedEventUri: Uri? = contentResolver.insert(EVENT_URI, values)

        if (savedEventUri != null) {
            calenderEntryCached.put(entryKey, event)
            return AddEventResult(Status.CREATED, event)
        } else {
            return AddEventResult(Status.ERROR, event)
        }
    }

}

class TestCalendarRepositoryImpl : CalendarRepository {
    private val calendars = listOf(
        Calendar(1, "abc@gmail.pl", "google.com", "abc@gmail.pl", "abc@gmail.pl"),
        Calendar(2, "abc@gmail.pl", "test.google.com", "Test calendar", "abc@gmail.pl"),
        Calendar(3, "Local calendar", "local", "Local calendar", "abc@gmail.pl"),
    )

    private val calendarEventColors = listOf(
        CalendarEventColor("1", "#ff445f59", "", calendars[0].accountName, calendars[0].accountType),
        CalendarEventColor("2", "#ffaacaa3", "", calendars[0].accountName, calendars[0].accountType),
        CalendarEventColor("3", "#ff660d11", "", calendars[0].accountName, calendars[0].accountType),
        CalendarEventColor("4", "#ff4b3294", "", calendars[0].accountName, calendars[0].accountType),
        CalendarEventColor("5", "#ffd67992", "", calendars[0].accountName, calendars[0].accountType),
        CalendarEventColor("6", "#ff157ca8", "", calendars[0].accountName, calendars[0].accountType),
        CalendarEventColor("7", "#ffb498d0", "", calendars[0].accountName, calendars[0].accountType),
        CalendarEventColor("8", "#fffefd4c", "", calendars[0].accountName, calendars[0].accountType),
        CalendarEventColor("9", "#ff5a3f76", "", calendars[0].accountName, calendars[0].accountType),
        CalendarEventColor("10", "#fffd08ff", "", calendars[0].accountName, calendars[0].accountType),

        CalendarEventColor("1", "#ff445f59", "", calendars[1].accountName, calendars[1].accountType),
        CalendarEventColor("2", "#ffaacaa3", "", calendars[1].accountName, calendars[1].accountType),
        CalendarEventColor("3", "#ff660d11", "", calendars[1].accountName, calendars[1].accountType),

        CalendarEventColor("1", "#ff445f59", "", calendars[2].accountName, calendars[2].accountType),
        CalendarEventColor("2", "#ffaacaa3", "", calendars[2].accountName, calendars[2].accountType),
        CalendarEventColor("3", "#ff660d11", "", calendars[2].accountName, calendars[2].accountType),
        CalendarEventColor("4", "#ff4b3294", "", calendars[2].accountName, calendars[2].accountType),
        CalendarEventColor("5", "#ffd67992", "", calendars[2].accountName, calendars[2].accountType),
        CalendarEventColor("6", "#ff157ca8", "", calendars[2].accountName, calendars[2].accountType),
        CalendarEventColor("7", "#ffb498d0", "", calendars[2].accountName, calendars[2].accountType),

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
        calendarEventColors.forEach{
            println("$it - ${it.colorAsLong()}")
        }
        return calendarEventColors
    }

    override suspend fun addEventToCalendar(
        calendar: Calendar,
        event: CalendarEvent
    ): AddEventResult {
        delay(2_000)
        calendarEvents.computeIfAbsent(calendar) { c -> mutableListOf() }
            .add(event)

        return AddEventResult(Status.CREATED, event)
    }

}