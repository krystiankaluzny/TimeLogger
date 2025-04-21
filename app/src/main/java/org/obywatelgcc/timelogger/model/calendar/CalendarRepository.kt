package org.obywatelgcc.timelogger.model.calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import org.obywatelgcc.timelogger.model.calendar.CalendarRepository.AddEventResult
import org.obywatelgcc.timelogger.model.calendar.CalendarRepository.AddEventResult.Status
import java.time.ZonedDateTime


interface CalendarRepository {

    suspend fun findAllClendars(): List<Calendar>
    suspend fun findAllEventColors(): List<CalendarEventColor>
    suspend fun addEventToCalendar(calendar: Calendar, entry: CalendarEvent): AddEventResult

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
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.OWNER_ACCOUNT
            )
        )

        private val colorsProjection: Projection = Projection(
            listOf(
                CalendarContract.Colors.COLOR_KEY,
                CalendarContract.Colors.COLOR,
                CalendarContract.Colors.COLOR_TYPE
            )
        )
    }

    override suspend fun findAllClendars(): List<Calendar> {
        val result = mutableListOf<Calendar>()

        result.add(Calendar(-1, "1", "abc", ""))
        result.add(Calendar(-2, "2", "abc", ""))
        result.add(Calendar(-3, "3", "def", ""))
        result.add(Calendar(-4, "4", "def", ""))
        result.add(Calendar(-5, "5", "def", ""))
        result.add(Calendar(-6, "6", "def", ""))

        val calenderCursor: Cursor? =
            contentResolver.query(CALENDAR_URI, calendarsProjection.projection, null, null, null)
        calenderCursor?.apply {
            while (moveToNext()) {
                result.add(
                    Calendar(
                        id = getLong(calendarsProjection.index(CalendarContract.Calendars._ID)),
                        accountName = getString(calendarsProjection.index(CalendarContract.Calendars.ACCOUNT_NAME)),
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
        val result = mutableListOf<CalendarEventColor>()

        result.add(CalendarEventColor("A", "0xFF00FF00", ""))
        result.add(CalendarEventColor("B", "0xFFFFFF00", ""))
        result.add(CalendarEventColor("C", "0xFF000FFF", ""))
        result.add(CalendarEventColor("D", "0xFFF0F0FF", ""))
        result.add(CalendarEventColor("E", "0xFF0FFFFF", ""))
        result.add(CalendarEventColor("F", "0xFFDDFF00", ""))
        result.add(CalendarEventColor("G", "0xFFF0F0F0", ""))
        result.add(CalendarEventColor("H", "0xFFF1FF0F", ""))
        result.add(CalendarEventColor("I", "0xFF0F0DDF", ""))
        result.add(CalendarEventColor("J", "0xFFFF000F", ""))

        val colorCursor: Cursor? =
            contentResolver.query(COLOR_URI, colorsProjection.projection, null, null, null)
        colorCursor?.apply {
            while (moveToNext()) {
                result.add(
                    CalendarEventColor(
                        key = getString(colorsProjection.index(CalendarContract.Colors.COLOR_KEY)),
                        color = getString(colorsProjection.index(CalendarContract.Colors.COLOR)),
                        type = getString(colorsProjection.index(CalendarContract.Colors.COLOR_TYPE)),
                    )
                )
            }
            close()
        }

        return result
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
            put(CalendarContract.Events.EVENT_COLOR_KEY, "")
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