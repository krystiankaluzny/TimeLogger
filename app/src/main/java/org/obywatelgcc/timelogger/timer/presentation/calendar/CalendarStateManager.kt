package org.obywatelgcc.timelogger.timer.presentation.calendar

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.settings.model.CalendarSettings
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

    suspend fun init(calendarSettings: CalendarSettings) {
        logInfo("init")
        calendarPreferences.loadFormDataStore()

        val colorsData = getOrCreateColorsData(calendarSettings.selectedCalendar, calendarSettings.availableColors)

        state.update {
            it.copy(
                availableColors = calendarSettings.availableColors,
                selectedColor = colorsData.selectedColor
            )
        }
    }

    fun updateCalendar(calendarSettings: CalendarSettings) {
        val colorsData = getOrCreateColorsData(calendarSettings.selectedCalendar, calendarSettings.availableColors)

        state.update {
            it.copy(
                availableColors = calendarSettings.availableColors,
                selectedColor = colorsData.selectedColor
            )
        }
    }

    fun selectColor(color: CalendarEventColor) {
        calendarPreferences.edit { pref ->
            pref.dataMap[pref.currentCalendarId]?.selectedColor = color
            pref
        }

        state.update {
            it.copy(selectedColor = color)
        }
    }

    fun selectSuggestedColor(color: CalendarEventColor?) {
        if (state.value.availableColors.contains(color)) {
            color?.let { selectColor(color) }
        }
    }

    fun shiftColor() {
        val colors = state.value.availableColors
        val currentColorIndex = colors.indexOf(state.value.selectedColor)

        if (currentColorIndex != -1) {
            if (currentColorIndex == colors.lastIndex) {
                selectColor(colors.first())
            } else {
                selectColor(colors[currentColorIndex + 1])
            }
        }
    }

    private fun getOrCreateColorsData(
        calendar: Calendar,
        availableColors: List<CalendarEventColor>
    ): CalendarColorsData {
        val existing = calendarPreferences.value.dataMap[calendar.id]
        val colorsData = if (existing != null) {
            if (!availableColors.contains(existing.selectedColor)) {
                existing.selectedColor = availableColors.getOrElse(0) { CalendarEventColor.Empty }
            }
            existing
        } else {
            CalendarColorsData(
                availableColors,
                availableColors.getOrElse(0) { CalendarEventColor.Empty }
            )
        }

        calendarPreferences.edit { pref ->
            pref.currentCalendarId = calendar.id
            pref.dataMap[calendar.id] = colorsData
            pref
        }

        return colorsData
    }
}


@Serializable
private data class CalendarPreferences(
    val dataMap: MutableMap<Long, CalendarColorsData> = mutableMapOf(),
    var currentCalendarId: Long = Calendar.Empty.id
)

@Serializable
private class CalendarColorsData(
    val colors: List<CalendarEventColor>,
    var selectedColor: CalendarEventColor
)
