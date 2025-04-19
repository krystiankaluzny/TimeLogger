package org.obywatelgcc.timelogger.viewmodel

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import org.obywatelgcc.timelogger.model.calendar.Calendar

class TimeCalendarState {

    val state = MutableStateFlow(State.BEFORE_INITIALIZING)
    val availableCalendars = MutableStateFlow(listOf<Calendar>())
    val selectedCalendar = MutableStateFlow<Calendar?>(null)

    fun init(calendars: List<Calendar>) {
        Log.d("Dupa", "init: $calendars")
        availableCalendars.value = calendars
        selectedCalendar.value = calendars.getOrNull(0)

        state.value = if (calendars.isNotEmpty()) State.SUCCESSFULLY_INITIALIZED else State.CALENDARS_NOT_FOUND
    }

    fun select(calendar: Calendar) {
        selectedCalendar.value = calendar
    }
}

enum class State {
    BEFORE_INITIALIZING, SUCCESSFULLY_INITIALIZED, CALENDARS_NOT_FOUND
}