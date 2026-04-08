package org.obywatelgcc.timelogger.core.presentation.filter

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.core.presentation.components.filter.FilterTimeRangeType
import org.obywatelgcc.timelogger.utils.logInfo
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

class FilterTimeRangeStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    initialState: FilterTimeRangeState,
    preferencesKey: String = "filtersPreferences_v1"
) : BaseStateManager<FilterTimeRangeState>(
    coroutineScope,
    savedStateHandle,
    dataStoreManager,
    initialState
) {

    private val filterPreferencesKey = preferencesKey
    private var filterPreferences = jsonDataStoreStateFlow(filterPreferencesKey, FilterPreferences())

    suspend fun init() {
        logInfo("init")

        filterPreferences.loadFormDataStore()

        state.update {
            it.copy(
                timeRangeType = filterPreferences.value.timeRangeType,
                timeRange = filterPreferences.value.timeRange
            )
        }

        selectTimeRangeType(filterPreferences.value.timeRangeType)
    }

    fun selectTimeRangeType(type: FilterTimeRangeType) {
        val now = ZonedDateTime.now()
        val newForm = when (type) {
            FilterTimeRangeType.DAY -> now.truncatedTo(ChronoUnit.DAYS)
            FilterTimeRangeType.WEEK -> now.with(WeekFields.ISO.firstDayOfWeek).truncatedTo(ChronoUnit.DAYS)
            FilterTimeRangeType.MONTH -> now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)
        }

        val newTo = when (type) {
            FilterTimeRangeType.DAY -> newForm.plusDays(1L)
            FilterTimeRangeType.WEEK -> newForm.plusDays(7L)
            FilterTimeRangeType.MONTH -> newForm.plusMonths(1L)
        }

        val timeRange = ZonedDateTimeRange(newForm, newTo)

        state.update {
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

        state.update {
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

        state.update {
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

    fun resetRange() {
        val type = state.value.timeRangeType

        val now = ZonedDateTime.now()
        val newForm = when (type) {
            FilterTimeRangeType.DAY -> now.truncatedTo(ChronoUnit.DAYS)
            FilterTimeRangeType.WEEK -> now.with(WeekFields.ISO.firstDayOfWeek).truncatedTo(ChronoUnit.DAYS)
            FilterTimeRangeType.MONTH -> now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS)
        }

        val newTo = when (type) {
            FilterTimeRangeType.DAY -> newForm.plusDays(1L)
            FilterTimeRangeType.WEEK -> newForm.plusDays(7L)
            FilterTimeRangeType.MONTH -> newForm.plusMonths(1L)
        }

        val timeRange = ZonedDateTimeRange(newForm, newTo)

        state.update {
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
    var timeRangeType: FilterTimeRangeType = FilterTimeRangeType.DAY,
    var timeRange: ZonedDateTimeRange = ZonedDateTimeRange(ZonedDateTime.now(), ZonedDateTime.now())
)
