package org.obywatelgcc.timelogger.statistics.presentation.stats

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.CalendarEvent
import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.statistics.presentation.stats.StatisticsState.StatisticItem
import org.obywatelgcc.timelogger.utils.logDebug
import java.time.Duration
import java.time.ZonedDateTime

class StatisticsStateManager(
    coroutineScope: CoroutineScope,
    savedStateHandle: SavedStateHandle,
    dataStoreManager: DataStoreManager,
    initialState: StatisticsState
) : BaseStateManager<StatisticsState>(
    coroutineScope,
    savedStateHandle,
    dataStoreManager,
    initialState
) {

    fun recalculate(queryTimeRange: ZonedDateTimeRange, calendarEvents: List<CalendarEvent>) {

        val statisticItems = calendarEvents.groupingBy { it.title }
            .aggregate { key: String, acc: DataHolder?, event: CalendarEvent, first: Boolean ->

                val dataHolder = if (first || acc == null) { DataHolder() } else acc

                val eventDuration = Duration.between(
                    max(event.timeRange.from, queryTimeRange.from),
                    min(event.timeRange.to, queryTimeRange.to)
                )

                if (!eventDuration.isNegative) {
                    dataHolder.totalDuration = dataHolder.totalDuration.plus(eventDuration)
                    dataHolder.color = event.color
                }

                dataHolder
            }
            .map {
                StatisticItem(it.key, it.value.totalDuration, it.value.color)
            }

        val sorted = statisticItems.sortedByDescending { it.totalDuration }

        val result = if(sorted.size > 5) {
            val othersDuration = sorted.subList(4, sorted.size)
                .fold(Duration.ZERO) { acc, item -> acc.plus(item.totalDuration) }

            sorted.subList(0, 4) + StatisticItem("Others", othersDuration, sorted[4].color)
        } else {
            sorted
        }

        state.update {
            it.copy(
                statisticItems = result
            )
        }
    }

    private fun min(a: ZonedDateTime, b: ZonedDateTime) = if (a.isBefore(b)) a else b
    private fun max(a: ZonedDateTime, b: ZonedDateTime) = if (a.isAfter(b)) a else b
}

data class DataHolder(
    var totalDuration: Duration = Duration.ZERO,
    var color: CalendarEventColor? = null
)

