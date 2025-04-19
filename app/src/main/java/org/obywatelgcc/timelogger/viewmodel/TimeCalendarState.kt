package org.obywatelgcc.timelogger.viewmodel

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import org.obywatelgcc.timelogger.model.calendar.Calendar

class TimeCalendarState {

    val availableCalendars = MutableStateFlow(listOf<Calendar>())
    val selectedCalendar = MutableStateFlow<Calendar?>(null)

    fun init(calendars: List<Calendar>) {
        availableCalendars.value = calendars
        selectedCalendar.value = calendars.getOrNull(0)
    }

    fun select(calendar: Calendar) {
        Log.d("Dupa", "select: $calendar")
        selectedCalendar.value = calendar
    }
}