package org.obywatelgcc.timelogger.model.calendar

import android.util.Log
import kotlinx.coroutines.delay

interface CalendarRepository {

    suspend fun findAll(): List<Calendar>
    suspend fun addEntryToCalendar(calendar: Calendar, entry: CalendarEntry)
}

class CalendarRepositoryImpl : CalendarRepository {

    override suspend fun findAll(): List<Calendar> {
        delay(2000)
        return listOf(
            Calendar(1, "obywatel", "obywatel", "obywatel"),
            Calendar(2, "test", "obywatel", "obywatel")
        )
    }

    override suspend fun addEntryToCalendar(
        calendar: Calendar,
        entry: CalendarEntry
    ) {
        Log.d("Dupa", "addEntryToCalendar: $calendar, $entry")
        delay(1000)
    }
}