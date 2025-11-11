package org.obywatelgcc.timelogger.statistics.presentation.filter

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.utils.logInfo

class FilterStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    initialState: FilterState
) : BaseStateManager<FilterState>(
    coroutineScope,
    savedStateHandle,
    dataStoreManager,
    initialState
) {

    private val calendarPreferencesKey = "calendarStatisticsPreferences"
    private var filterPreferences =
        jsonDataStoreStateFlow(calendarPreferencesKey, FilterPreferences())

    suspend fun init(calendars: List<Calendar>) {
        logInfo("init")

        filterPreferences.loadFormDataStore()
        updatePreferences(calendars)

        state.update {
            it.copy(
                availableCalendars = calendars,
                selectedCalendar = filterPreferences.value.selectedCalendar,
            )
        }
    }

    fun selectCalendar(calendar: Calendar) {
        filterPreferences.edit { pref ->
            pref.selectedCalendar = calendar
            pref
        }

        return state.update { it.copy(selectedCalendar = calendar) }
    }


    private fun updatePreferences(calendars: List<Calendar>) {
        filterPreferences.edit { pref ->
            if (!calendars.contains(pref.selectedCalendar)) {
                pref.selectedCalendar = calendars.getOrElse(0, { Calendar.Empty })
            }

            pref
        }
    }

    fun selectTimeRangeType(type: FilterTimeRangeType) {
        return state.update { it.copy(timeRangeType = type) }
        filterPreferences.edit { pref ->
            pref.timeRangeType = type
            pref
        }
    }
}

@Serializable
private data class FilterPreferences(
    val calendars: List<Calendar> = mutableListOf<Calendar>(),
    var selectedCalendar: Calendar = Calendar.Empty,
    var timeRangeType: FilterTimeRangeType = FilterTimeRangeType.DAY
)