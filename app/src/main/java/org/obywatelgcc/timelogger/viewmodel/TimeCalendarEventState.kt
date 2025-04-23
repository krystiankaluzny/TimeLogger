package org.obywatelgcc.timelogger.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.model.calendar.Calendar
import org.obywatelgcc.timelogger.model.calendar.CalendarEventColor
import org.obywatelgcc.timelogger.model.calendar.DataStoreManager
import kotlin.reflect.typeOf

class TimeCalendarEventState(
    private val coroutineScope: CoroutineScope,
    private val savedStateHandle: SavedStateHandle,
    private val dataSoreManager: DataStoreManager
) {

    private val calendarPreferencesKey = "calendarPreferences"
    private lateinit var calendarPreferences: CalendarPreferences

    val state = MutableStateFlow(State.BEFORE_INITIALIZING)

    val availableCalendars =
        MutableSaveStateFlow(savedStateHandle, "availableCalendars", listOf<Calendar>())
    val selectedCalendar =
        MutableSaveStateFlow<Calendar?>(savedStateHandle, "selectedCalendar", null)

    val eventTitle = MutableSaveStateFlow(savedStateHandle, "eventTitle", "")

    val availableColors =
        MutableSaveStateFlow(savedStateHandle, "availableColors", listOf<CalendarEventColor>())
    val selectedColor =
        MutableSaveStateFlow<CalendarEventColor?>(savedStateHandle, "selectedColor", null)

    fun init(calendars: List<Calendar>, eventColors: List<CalendarEventColor>) {

        updateDataFromPreferences(calendars, eventColors)

        Log.d("TimeCalendarEventState", "init: before: $calendarPreferences")

        Log.d("TimeCalendarEventState", "init: start")
        availableCalendars.value = calendars
        selectedCalendar.value = calendarPreferences.selectedCalendar

        calendarPreferences.selectedCalendar?.let {
            val colorsData = getColorsData(it)
            availableColors.value = colorsData.colors
            selectedColor.value = colorsData.selectedColor
        }

        state.value =
            if (calendars.isNotEmpty()) State.SUCCESSFULLY_INITIALIZED else State.CALENDARS_NOT_FOUND
    }


    fun select(calendar: Calendar) {
        selectedCalendar.value = calendar
        calendarPreferences.selectedCalendar = calendar

        val colorsData = getColorsData(calendar)
        availableColors.value = colorsData.colors
        selectedColor.value = colorsData.selectedColor

        saveCalendarPreferences()
    }

    fun selectColor(color: CalendarEventColor) {
        selectedColor.value = color
        selectedCalendar.value?.let {
            getColorsData(it).selectedColor = color
        }

        saveCalendarPreferences()
    }

    fun updateTitle(title: String) {
        eventTitle.value = title
    }

    private fun updateDataFromPreferences(
        calendars: List<Calendar>,
        eventColors: List<CalendarEventColor>
    ) {
        loadCalendarPreferences()

        if (calendarPreferences.selectedCalendar == null || !calendars.contains(calendarPreferences.selectedCalendar)) {
            calendarPreferences.selectedCalendar = calendars.getOrNull(0)
        }

        val dataMap = calendarPreferences.dataMap
        val calendarIds = calendars.map { it.id }
        dataMap.entries.removeIf { !calendarIds.contains(it.key) }

        calendars.forEach { calendar ->
            val availableColors = eventColors
                .filter {
                    calendar.accountName == it.accountName
                            && calendar.accountType == it.accountType
                }

            val data = dataMap.getOrPut(calendar.id) {
                CalendarColorsData(
                    calendar,
                    availableColors,
                    null
                )
            }

            if (data.selectedColor == null || !availableColors.contains(data.selectedColor)) {
                data.selectedColor = availableColors.getOrNull(0)
            }
        }

        saveCalendarPreferences()
    }

    private fun getColorsData(calendar: Calendar): CalendarColorsData =
        calendarPreferences.dataMap.getValue(calendar.id)

    private fun loadCalendarPreferences() {
        runBlocking {
            calendarPreferences = dataSoreManager.getFromJson<CalendarPreferences>(
                calendarPreferencesKey,
                typeOf<CalendarPreferences>()
            ).first() ?: CalendarPreferences()
        }
    }

    private fun saveCalendarPreferences() {
        coroutineScope.launch {
            dataSoreManager.saveAsJson(
                calendarPreferencesKey,
                calendarPreferences,
                typeOf<CalendarPreferences>()
            )
        }
    }
}

enum class State {
    BEFORE_INITIALIZING, SUCCESSFULLY_INITIALIZED, CALENDARS_NOT_FOUND
}

@Serializable
private data class CalendarPreferences(
    val dataMap: MutableMap<Long, CalendarColorsData> = mutableMapOf<Long, CalendarColorsData>(),
    var selectedCalendar: Calendar? = null
)

@Serializable
private class CalendarColorsData(
    val calendar: Calendar,
    val colors: List<CalendarEventColor>,
    var selectedColor: CalendarEventColor?
)