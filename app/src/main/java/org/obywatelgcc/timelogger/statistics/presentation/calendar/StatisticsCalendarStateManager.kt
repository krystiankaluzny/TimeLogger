package org.obywatelgcc.timelogger.statistics.presentation.calendar

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.utils.logInfo

class StatisticsCalendarStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    initialState: StatisticsCalendarState
) : BaseStateManager<StatisticsCalendarState>(
    coroutineScope,
    savedStateHandle,
    dataStoreManager,
    initialState
) {

    private val calendarPreferencesKey = "calendarStatisticsPreferences"
    private var statisticsCalendarPreferences =
        jsonDataStoreStateFlow(calendarPreferencesKey, StatisticsCalendarPreferences())

    suspend fun init(calendars: List<Calendar>) {
        logInfo("init")

        statisticsCalendarPreferences.loadFormDataStore()
        updatePreferences(calendars)

        state.update {
            it.copy(
                availableCalendars = calendars,
                selectedCalendar = statisticsCalendarPreferences.value.selectedCalendar,
            )
        }
    }

    fun selectCalendar(calendar: Calendar) {
        statisticsCalendarPreferences.edit { pref ->
            pref.selectedCalendar = calendar
            pref
        }

        return state.update { it.copy(selectedCalendar = calendar) }
    }


    private fun updatePreferences(calendars: List<Calendar>) {
        statisticsCalendarPreferences.edit { pref ->
            if (!calendars.contains(pref.selectedCalendar)) {
                pref.selectedCalendar = calendars.getOrElse(0, { Calendar.Empty })
            }

            pref
        }
    }
}


@Serializable
private data class StatisticsCalendarPreferences(
    val calendars: List<Calendar> = mutableListOf<Calendar>(),
    var selectedCalendar: Calendar = Calendar.Empty
)