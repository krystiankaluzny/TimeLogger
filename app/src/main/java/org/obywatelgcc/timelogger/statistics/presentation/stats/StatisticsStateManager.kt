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

    companion object {
        const val OTHERS_LABEL = "Others"
        const val OTHERS_THRESHOLD = 9
    }

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

        val result = if(sorted.size > OTHERS_THRESHOLD) {
            val othersDuration = sorted.subList(OTHERS_THRESHOLD, sorted.size)
                .fold(Duration.ZERO) { acc, item -> acc.plus(item.totalDuration) }

            sorted.subList(0, OTHERS_THRESHOLD) + StatisticItem(OTHERS_LABEL, othersDuration, sorted[OTHERS_THRESHOLD].color)
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

