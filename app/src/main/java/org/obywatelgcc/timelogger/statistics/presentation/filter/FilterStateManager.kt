package org.obywatelgcc.timelogger.statistics.presentation.filter

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.Calendar
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.utils.logInfo
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

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

    private val filterPreferencesKey = "calendarStatisticsPreferences_v2"
    private var filterPreferences =
        jsonDataStoreStateFlow(filterPreferencesKey, FilterPreferences())

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
        val oldFrom = state.value.timeRange.from

        val newForm = when (type) {
            FilterTimeRangeType.DAY -> oldFrom.truncatedTo(ChronoUnit.DAYS)
            FilterTimeRangeType.WEEK -> oldFrom.with(WeekFields.ISO.firstDayOfWeek).truncatedTo(ChronoUnit.DAYS)
            FilterTimeRangeType.MONTH -> oldFrom.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)
        }

        val newTo = when (type) {
            FilterTimeRangeType.DAY -> newForm.plusDays(1L)
            FilterTimeRangeType.WEEK -> newForm.plusDays(7L)
            FilterTimeRangeType.MONTH -> newForm.plusMonths(1L)
        }

        val timeRange = ZonedDateTimeRange(newForm, newTo)

        return state.update {
            it.copy(
                timeRangeType = type,
                timeRange = timeRange
            )
        }
        filterPreferences.edit { pref ->
            pref.timeRangeType = type
            pref.timeRange = timeRange
            pref
        }
    }

    fun calculatePreviousRange() {
        val type = state.value.timeRangeType
        val oldFrom = state.value.timeRange.from
        val oldTo = state.value.timeRange.to

        val newForm = when (type) {
            FilterTimeRangeType.DAY -> oldFrom.minusDays(1L)
            FilterTimeRangeType.WEEK -> oldFrom.minusDays(7L)
            FilterTimeRangeType.MONTH -> oldFrom.minusMonths(1L)
        }

        val newTo = when (type) {
            FilterTimeRangeType.DAY -> oldTo.minusDays(1L)
            FilterTimeRangeType.WEEK -> oldTo.minusDays(7L)
            FilterTimeRangeType.MONTH -> oldTo.minusMonths(1L)
        }

        val timeRange = ZonedDateTimeRange(newForm, newTo)

        return state.update {
            it.copy(
                timeRangeType = type,
                timeRange = timeRange
            )
        }
        filterPreferences.edit { pref ->
            pref.timeRangeType = type
            pref.timeRange = timeRange
            pref
        }
    }

    fun calculateNextRange() {
        val type = state.value.timeRangeType
        val oldFrom = state.value.timeRange.from
        val oldTo = state.value.timeRange.to

        val newForm = when (type) {
            FilterTimeRangeType.DAY -> oldFrom.plusDays(1L)
            FilterTimeRangeType.WEEK -> oldFrom.plusDays(7L)
            FilterTimeRangeType.MONTH -> oldFrom.plusMonths(1L)
        }

        val newTo = when (type) {
            FilterTimeRangeType.DAY -> oldTo.plusDays(1L)
            FilterTimeRangeType.WEEK -> oldTo.plusDays(7L)
            FilterTimeRangeType.MONTH -> oldTo.plusMonths(1L)
        }

        val timeRange = ZonedDateTimeRange(newForm, newTo)

        return state.update {
            it.copy(
                timeRangeType = type,
                timeRange = timeRange
            )
        }
        filterPreferences.edit { pref ->
            pref.timeRangeType = type
            pref.timeRange = timeRange
            pref
        }
    }
}

@Serializable
private data class FilterPreferences(
    var calendars: List<Calendar> = mutableListOf<Calendar>(),
    var selectedCalendar: Calendar = Calendar.Empty,
    var timeRangeType: FilterTimeRangeType = FilterTimeRangeType.DAY,
    var timeRange: ZonedDateTimeRange = ZonedDateTimeRange(ZonedDateTime.now(), ZonedDateTime.now())
)