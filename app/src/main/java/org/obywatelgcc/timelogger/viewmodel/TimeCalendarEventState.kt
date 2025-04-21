package org.obywatelgcc.timelogger.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import org.obywatelgcc.timelogger.model.calendar.Calendar
import org.obywatelgcc.timelogger.model.calendar.CalendarEventColor

class TimeCalendarEventState {

    val state = MutableStateFlow(State.BEFORE_INITIALIZING)

    val availableCalendars = MutableStateFlow(listOf<Calendar>())
    val selectedCalendar = MutableStateFlow<Calendar?>(null)

    val eventTitle = MutableStateFlow("")

    val allEventColors = MutableStateFlow(listOf<CalendarEventColor>())
    val availableColors = MutableStateFlow(listOf<CalendarEventColor>())
    val selectedColor = MutableStateFlow<CalendarEventColor?>(null)

    fun init(calendars: List<Calendar>, eventColors: List<CalendarEventColor>) {
        availableCalendars.value = calendars
        selectedCalendar.value = calendars.getOrNull(0)

        allEventColors.value = eventColors
        selectedCalendar.value?.let { refreshAvailableColorsForCalendar(it) }

        state.value =
            if (calendars.isNotEmpty()) State.SUCCESSFULLY_INITIALIZED else State.CALENDARS_NOT_FOUND
    }

    fun select(calendar: Calendar) {
        selectedCalendar.value = calendar
        refreshAvailableColorsForCalendar(calendar)
    }

    fun selectColor(color: CalendarEventColor) {
        selectedColor.value = color
    }

    fun updateTitle(title: String) {
        eventTitle.value = title
    }

    private fun refreshAvailableColorsForCalendar(calendar: Calendar) {
        availableColors.value = allEventColors.value
            .filter { calendar.accountName == it.accountName && calendar.accountType == it.accountType}
        selectedColor.value = availableColors.value.getOrNull(0)
    }
}

enum class State {
    BEFORE_INITIALIZING, SUCCESSFULLY_INITIALIZED, CALENDARS_NOT_FOUND
}