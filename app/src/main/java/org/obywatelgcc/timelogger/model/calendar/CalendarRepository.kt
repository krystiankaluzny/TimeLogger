package org.obywatelgcc.timelogger.model.calendar

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import org.obywatelgcc.timelogger.model.calendar.CalendarRepository.AddEntryResult
import org.obywatelgcc.timelogger.model.calendar.CalendarRepository.AddEntryResult.Status
import java.time.ZonedDateTime


interface CalendarRepository {

    suspend fun findAll(): List<Calendar>
    suspend fun addEntryToCalendar(calendar: Calendar, entry: CalendarEntry): AddEntryResult

    data class AddEntryResult(
        val status: Status,
        val entry: CalendarEntry
    ) {
        enum class Status { ALREADY_EXISTS, CREATED, ERROR }
    }
}

class CalendarRepositoryImpl(
    androidContext: Context
) : CalendarRepository {

    val contentResolver: ContentResolver = androidContext.contentResolver

    private var calenderEntryCached = mutableMapOf<EntryKey, CalendarEntry>()

    private data class EntryKey(val title: String, val start: ZonedDateTime)

    companion object {
        val CALENDAR_URI: Uri = CalendarContract.Calendars.CONTENT_URI
        val EVENT_URI: Uri = CalendarContract.Events.CONTENT_URI

        val PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,                     // 0
            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
            CalendarContract.Calendars.OWNER_ACCOUNT            // 3
        )

        // The indices for the projection array above.
        const val PROJECTION_ID_INDEX: Int = 0
        const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
        const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
        const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3
    }

    override suspend fun findAll(): List<Calendar> {
        val result = mutableListOf<Calendar>()

        val calenderCursor: Cursor? =
            contentResolver.query(CALENDAR_URI, PROJECTION, null, null, null)
        calenderCursor?.apply {
            while (moveToNext()) {
                result.add(
                    Calendar(
                        id = getLong(PROJECTION_ID_INDEX),
                        accountName = getString(PROJECTION_ACCOUNT_NAME_INDEX),
                        displayName = getString(PROJECTION_DISPLAY_NAME_INDEX),
                        ownerName = getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                    )
                )
            }
            close()
        }

        return result
    }

    override suspend fun addEntryToCalendar(
        calendar: Calendar,
        entry: CalendarEntry
    ): AddEntryResult {
        Log.d("CalendarRepository", "addEntryToCalendar: $calendar, $entry")

        val entryKey = EntryKey(entry.title, entry.start)
        val cachedEntry = calenderEntryCached[entryKey]
        if (cachedEntry != null) {
            return AddEntryResult(Status.ALREADY_EXISTS, entry)
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, entry.startMillis())
            put(CalendarContract.Events.DTEND, entry.endMillis())
            put(CalendarContract.Events.TITLE, entry.title)
            put(CalendarContract.Events.DESCRIPTION, "")
            put(CalendarContract.Events.CALENDAR_ID, calendar.id)
            put(CalendarContract.Events.EVENT_TIMEZONE, entry.zoneIdName())
        }

        Log.d("CalendarRepository", "addEntryToCalendar: $values")

        val eventUri: Uri? = contentResolver.insert(EVENT_URI, values)

        if (eventUri != null) {
            calenderEntryCached.put(entryKey, entry)
            return AddEntryResult(Status.CREATED, entry)
        } else {
            return AddEntryResult(Status.ERROR, entry)
        }
    }

}