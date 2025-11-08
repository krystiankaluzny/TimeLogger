package org.obywatelgcc.timelogger.core.model

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.database.getStringOrNull
import org.obywatelgcc.timelogger.utils.logDebug
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


class CalendarRepositoryImpl(
    androidContext: Context
) : CalendarRepository {

    val contentResolver: ContentResolver = androidContext.contentResolver

    private var calenderEntryCached = mutableMapOf<EntryKey, CalendarEvent>()
    private var lastLoadedCalendars = listOf<Calendar>()
    private var lastLoadedColors = setOf<CalendarEventColor>()

    private data class EntryKey(val calendarId: Long, val title: String, val start: ZonedDateTime)

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
        private val eventsProjection: Projection = Projection(
            listOf(
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.EVENT_TIMEZONE,
                CalendarContract.Events.EVENT_COLOR_KEY
            )
        )
    }

    override suspend fun findAllCalendars(): List<Calendar> {
        logDebug("findAllCalendars")

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

        lastLoadedCalendars = result as List<Calendar>
        return result
    }

    override suspend fun findAllEventColors(): List<CalendarEventColor> {
        logDebug("findAllEventColors")

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

        logDebug("findAllEventColors: $result")

        lastLoadedColors = result as Set<CalendarEventColor>

        return result.toList()
    }

    override suspend fun addEventToCalendar(
        calendar: Calendar,
        event: CalendarEvent
    ): CalendarRepository.AddEventResult {

        logDebug("addEventToCalendar: $calendar, $event")

        val eventTimeRange = event.timeRange
        val entryKey = EntryKey(calendar.id, event.title, eventTimeRange.from)
        val cachedEntry = calenderEntryCached[entryKey]
        if (cachedEntry != null) {
            return CalendarRepository.AddEventResult(CalendarRepository.AddEventResult.Status.ALREADY_EXISTS, event)
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, eventTimeRange.fromMillis())
            put(CalendarContract.Events.DTEND, eventTimeRange.toMillis())
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, "")
            put(CalendarContract.Events.CALENDAR_ID, calendar.id)
            put(CalendarContract.Events.EVENT_TIMEZONE, eventTimeRange.zoneIdName())
            put(CalendarContract.Events.EVENT_COLOR_KEY, event.color?.key ?: "")
        }

        logDebug("addEventToCalendar: $values")

        val savedEventUri: Uri? = contentResolver.insert(EVENT_URI, values)

        if (savedEventUri != null) {
            calenderEntryCached.put(entryKey, event)
            return CalendarRepository.AddEventResult(CalendarRepository.AddEventResult.Status.CREATED, event)
        } else {
            return CalendarRepository.AddEventResult(CalendarRepository.AddEventResult.Status.ERROR, event)
        }
    }

    override suspend fun findEventsContainsTitle(
        calendar: Calendar,
        title: String
    ): List<CalendarEvent> {


        val now = Instant.now().toEpochMilli()
        val from = Instant.now().minus(100, ChronoUnit.DAYS).toEpochMilli()

        val selection =
            """(
                | ${CalendarContract.Events.CALENDAR_ID} = ?
                | AND ${CalendarContract.Events.DTSTART} >= ?
                | AND ${CalendarContract.Events.DTSTART} <= ? 
                | AND ${CalendarContract.Events.TITLE} LIKE '%$title%'
                | )""".trimMargin()
        val selectionArgs = arrayOf(calendar.id.toString(), from.toString(), now.toString())

        var result = queryEvents(calendar, selection, selectionArgs)

        logDebug("findEventsContainsTitle: size: ${result.size} - $result")

        if (result.isEmpty()) {
            result = calenderEntryCached.entries
                .filter { e -> e.key.calendarId == calendar.id && e.value.title.contains(title, true) }
                .map { e -> e.value }

            logDebug("findEventsContainsTitle: cache: size: ${result.size} - $result")
        }

        return result
    }


    override suspend fun findEventsInTimeRange(
        calendar: Calendar,
        timeRange: ZonedDateTimeRange
    ): List<CalendarEvent> {


        val from = timeRange.fromMillis()
        val to = timeRange.toMillis()

        val selection =
            """(
                | ${CalendarContract.Events.CALENDAR_ID} = ?
                | AND ${CalendarContract.Events.DTSTART} <= ?
                | AND ${CalendarContract.Events.DTEND} >= ? 
                | )""".trimMargin()
        val selectionArgs = arrayOf(calendar.id.toString(), to.toString(), from.toString())

        return queryEvents(calendar, selection, selectionArgs)
    }

    private fun queryEvents(calendar: Calendar, selection: String, selectionArgs: Array<String>): List<CalendarEvent> {
        val result = mutableListOf<CalendarEvent>()

        val eventCursor: Cursor? =
            contentResolver.query(EVENT_URI, eventsProjection.projection, selection, selectionArgs, null)
        eventCursor?.apply {
            while (moveToNext()) {
                val eventColorKey = getString(eventsProjection.index(CalendarContract.Events.EVENT_COLOR_KEY))

                val color =
                    lastLoadedColors.firstOrNull {
                        it.accountName == calendar.accountName
                                && it.accountType == calendar.accountType
                                && it.key == eventColorKey
                    }

                result.add(
                    CalendarEvent.of(
                        getStringOrNull(eventsProjection.index(CalendarContract.Events.TITLE)) ?: "",
                        getLong(eventsProjection.index(CalendarContract.Events.DTSTART)),
                        getLong(eventsProjection.index(CalendarContract.Events.DTEND)),
                        getStringOrNull(eventsProjection.index(CalendarContract.Events.EVENT_TIMEZONE)) ?: "UTC",
                        color
                    )
                )
            }
            close()
        }

        return result
    }
}

