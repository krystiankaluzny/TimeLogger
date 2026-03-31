package org.obywatelgcc.timelogger.statistics.presentation.stats

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.model.CalendarEvent
import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.settings.model.SleepWindowSettings
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
        val OTHERS_COLOR = CalendarEventColor("Others", "#FF0101010", "", "", "")
        const val SLEEP_LABEL = "Sleep"
        val SLEEP_COLOR = CalendarEventColor("Sleep", "#FFE06707", "", "", "")
        const val OTHERS_THRESHOLD = 40
    }

    fun recalculate(
        queryTimeRange: ZonedDateTimeRange,
        calendarEvents: List<CalendarEvent>,
        sleepWindowSettings: SleepWindowSettings
    ) {
        val statisticItems = calendarEvents.groupingBy { it.title }
            .aggregate { key: String, acc: DataHolder?, event: CalendarEvent, first: Boolean ->

                val dataHolder = if (first || acc == null) { DataHolder() } else acc

                //Cut event duration to query time range
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
            .toMutableList()

        if (sleepWindowSettings.enabled) {
            val sleepDuration = calculateSleepDuration(queryTimeRange, calendarEvents, sleepWindowSettings)
            if (!sleepDuration.isZero) {
                statisticItems.add(StatisticItem(SLEEP_LABEL, sleepDuration, SLEEP_COLOR))
            }
        }

        val result = statisticItems.sortedByDescending { it.totalDuration }

        state.update {
            it.copy(
                statisticItems = result
            )
        }
    }

    private fun calculateSleepDuration(
        queryTimeRange: ZonedDateTimeRange,
        events: List<CalendarEvent>,
        sleepWindowSettings: SleepWindowSettings
    ): Duration {
        var totalSleep = Duration.ZERO
        val zone = queryTimeRange.from.zone
        var day = queryTimeRange.from.toLocalDate().minusDays(1)

        while (true) {
            val nightWindowStart = day.atTime(sleepWindowSettings.start).atZone(zone)
            val nightWindowEnd = day.plusDays(1).atTime(sleepWindowSettings.end).atZone(zone)

            if (nightWindowStart >= queryTimeRange.to) break

            val effectiveWindowStart = max(nightWindowStart, queryTimeRange.from)
            val effectiveWindowEnd = min(nightWindowEnd, queryTimeRange.to)

            if (effectiveWindowEnd > effectiveWindowStart) {
                val lastEvent = events
                    .filter { it.timeRange.to <= nightWindowEnd }
                    .maxByOrNull { it.timeRange.to }

                val firstEvent = events
                    .filter { it.timeRange.from >= nightWindowStart }
                    .minByOrNull { it.timeRange.from }

                val gapStart = if (lastEvent != null)
                    max(lastEvent.timeRange.to, effectiveWindowStart)
                else
                    effectiveWindowStart

                val gapEnd = if (firstEvent != null)
                    min(firstEvent.timeRange.from, effectiveWindowEnd)
                else
                    effectiveWindowEnd

                if (gapEnd > gapStart) {
                    totalSleep = totalSleep.plus(Duration.between(gapStart, gapEnd))
                }
            }

            day = day.plusDays(1)
        }

        return totalSleep
    }

    private fun min(a: ZonedDateTime, b: ZonedDateTime) = if (a.isBefore(b)) a else b
    private fun max(a: ZonedDateTime, b: ZonedDateTime) = if (a.isAfter(b)) a else b
}

data class DataHolder(
    var totalDuration: Duration = Duration.ZERO,
    var color: CalendarEventColor? = null
)
