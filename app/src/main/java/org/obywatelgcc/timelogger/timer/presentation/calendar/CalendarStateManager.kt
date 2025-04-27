package org.obywatelgcc.timelogger.timer.presentation.calendar

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.timer.model.Calendar
import org.obywatelgcc.timelogger.timer.model.CalendarEventColor
import org.obywatelgcc.timelogger.utils.logInfo

class CalendarStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    initialState: CalendarState
) : BaseStateManager<CalendarState>(
    coroutineScope,
    savedStateHandle,
    dataStoreManager,
    initialState
) {

    private val calendarPreferencesKey = "calendarPreferences"
    private var calendarPreferences =
        jsonDataStoreStateFlow(calendarPreferencesKey, CalendarPreferences())

    suspend fun init(
        calendars: List<Calendar>,
        eventColors: List<CalendarEventColor>
    ) {
        logInfo("init")

        calendarPreferences.loadFormDataStore()
        updatePreferences(calendars, eventColors)

        val colorsData = calendarPreferences.value.selectedCalendar?.let { getColorsData(it) }

        val availableColors = colorsData?.colors ?: emptyList()
        val selectedColor = colorsData?.selectedColor ?: CalendarEventColor.Empty

        state.update {
            it.copy(
                availableCalendars = calendars,
                selectedCalendar = calendarPreferences.value.selectedCalendar ?: Calendar.Empty,
                availableColors = availableColors,
                selectedColor = selectedColor
            )
        }
    }

    fun selectCalendar(calendar: Calendar) {
        val colorsData = getColorsData(calendar)

        calendarPreferences.edit { pref ->
            pref.selectedCalendar = calendar
            pref
        }

        return state.update {
            it.copy(
                selectedCalendar = calendar,
                availableColors = colorsData.colors,
                selectedColor = colorsData.selectedColor
            )
        }
    }


    fun selectColor(color: CalendarEventColor) {
        calendarPreferences.edit { pref ->
            getColorsData(pref.selectedCalendar).selectedColor = color
            pref
        }

        state.update {
            it.copy(
                selectedColor = color
            )
        }
    }

    private fun updatePreferences(
        calendars: List<Calendar>,
        eventColors: List<CalendarEventColor>
    ) {
        calendarPreferences.edit { pref ->
            if (!calendars.contains(pref.selectedCalendar)) {
                pref.selectedCalendar = calendars.getOrElse(0, { Calendar.Empty })
            }

            val dataMap = pref.dataMap
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
                        CalendarEventColor.Empty
                    )
                }

                if (!availableColors.contains(data.selectedColor)) {
                    data.selectedColor = availableColors.getOrElse(0, { CalendarEventColor.Empty })
                }
            }

            if(pref.selectedCalendar == Calendar.Empty) {
                dataMap.getOrPut(Calendar.Empty.id) {
                    CalendarColorsData(
                        Calendar.Empty,
                        emptyList(),
                        CalendarEventColor.Empty
                    )
                }
            }

            pref
        }
    }

    private fun getColorsData(calendar: Calendar): CalendarColorsData =
        calendarPreferences.value.dataMap.getValue(calendar.id)
}


@Serializable
private data class CalendarPreferences(
    val dataMap: MutableMap<Long, CalendarColorsData> = mutableMapOf<Long, CalendarColorsData>(),
    var selectedCalendar: Calendar = Calendar.Empty
)

@Serializable
private class CalendarColorsData(
    val calendar: Calendar,
    val colors: List<CalendarEventColor>,
    var selectedColor: CalendarEventColor
)