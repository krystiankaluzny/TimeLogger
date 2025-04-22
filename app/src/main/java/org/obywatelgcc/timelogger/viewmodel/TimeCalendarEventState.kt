package org.obywatelgcc.timelogger.viewmodel

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.Parcelize
import org.obywatelgcc.timelogger.model.calendar.Calendar
import org.obywatelgcc.timelogger.model.calendar.CalendarEventColor

class TimeCalendarEventState(private val savedStateHandle: SavedStateHandle) {

    val state = MutableStateFlow(State.BEFORE_INITIALIZING)

    val availableCalendars =
        MutableSaveStateFlow(savedStateHandle, "availableCalendars", listOf<Calendar>())
    val selectedCalendar =
        MutableSaveStateFlow<Calendar?>(savedStateHandle, "selectedCalendar", null)

    val eventTitle = MutableSaveStateFlow(savedStateHandle, "eventTitle", "")

    val calenderColorMap = mutableMapOf<Calendar, CalendarColorsData>()

    val allEventColors =
        MutableSaveStateFlow(savedStateHandle, "allEventColors", listOf<CalendarEventColor>())
    val availableColors =
        MutableSaveStateFlow(savedStateHandle, "availableColors", listOf<CalendarEventColor>())
    val selectedColor =
        MutableSaveStateFlow<CalendarEventColor?>(savedStateHandle, "selectedColor", null)

    fun init(calendars: List<Calendar>, eventColors: List<CalendarEventColor>) {

        Log.d("TimeCalendarEventState", "init: before: ${savedStateHandle.keys()}")
        Log.d("TimeCalendarEventState", "init: before: ${availableCalendars.value}")
        Log.d("TimeCalendarEventState", "init: before: ${selectedCalendar.value}")
        Log.d("TimeCalendarEventState", "init: before: ${eventTitle.value}")
        Log.d("TimeCalendarEventState", "init: before: ${selectedColor.value}")

        Log.d("TimeCalendarEventState", "init: start")
        availableCalendars.value = calendars

        if (selectedCalendar.value == null || !calendars.contains(selectedCalendar.value)) {
            selectedCalendar.value = calendars.getOrNull(0)
        }

        allEventColors.value = eventColors
        selectedCalendar.value?.let {
            val colorsData = getColorsData(it)
            availableColors.value = colorsData.colors
            if (selectedColor.value == null || !colorsData.colors.contains(selectedColor.value)) {
                selectedColor.value = colorsData.selectedColor
            } else {
                colorsData.selectedColor = selectedColor.value
            }
        }

        state.value =
            if (calendars.isNotEmpty()) State.SUCCESSFULLY_INITIALIZED else State.CALENDARS_NOT_FOUND
    }

    fun select(calendar: Calendar) {
        selectedCalendar.value = calendar
        refreshAvailableColorsForCalendar(calendar)
    }

    fun selectColor(color: CalendarEventColor) {
        selectedColor.value = color
        selectedCalendar.value?.let {
            getColorsData(it).selectedColor = color
        }
    }

    fun updateTitle(title: String) {
        eventTitle.value = title
    }

    private fun refreshAvailableColorsForCalendar(calendar: Calendar) {
        val colorsData = getColorsData(calendar)
        availableColors.value = colorsData.colors
        selectedColor.value = colorsData.selectedColor
    }

    private fun getColorsData(calendar: Calendar): CalendarColorsData {
        return calenderColorMap.getOrPut(calendar) {
            val availableColors = allEventColors.value
                .filter {
                    calendar.accountName == it.accountName
                            && calendar.accountType == it.accountType
                }

            CalendarColorsData(
                calendar, availableColors, availableColors.getOrNull(0)
            )
        }
    }
}

enum class State {
    BEFORE_INITIALIZING, SUCCESSFULLY_INITIALIZED, CALENDARS_NOT_FOUND
}


@Parcelize
class CalendarColorsData(
    val calendar: Calendar,
    val colors: List<CalendarEventColor>,
    var selectedColor: CalendarEventColor?
) : Parcelable