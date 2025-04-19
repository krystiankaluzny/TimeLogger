package org.obywatelgcc.timelogger.model.calendar

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import kotlinx.coroutines.delay

interface CalendarRepository {

    suspend fun findAll(): List<Calendar>
    suspend fun addEntryToCalendar(calendar: Calendar, entry: CalendarEntry)
}

class CalendarRepositoryImpl(
    private val androidContext: Context
) : CalendarRepository {

    companion object {
        val URI: Uri = CalendarContract.Calendars.CONTENT_URI

        val PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,                     // 0
            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
            CalendarContract.Calendars.OWNER_ACCOUNT            // 3
        )

        // The indices for the projection array above.
        val PROJECTION_ID_INDEX: Int = 0
        val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
        val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
        val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3
    }

    override suspend fun findAll(): List<Calendar> {
        val result = mutableListOf<Calendar>()

        val calenderCursor: Cursor? =
            androidContext.contentResolver.query(URI, PROJECTION, null, null, null)
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
    ) {
        Log.d("Dupa", "addEntryToCalendar: $calendar, $entry")
        delay(1000)
    }
}