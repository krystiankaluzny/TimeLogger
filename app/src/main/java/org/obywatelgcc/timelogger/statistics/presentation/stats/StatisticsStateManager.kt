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

                val dataHolder = if (first || acc == null) {
                    DataHolder()
                } else acc

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
        val now = ZonedDateTime.now()
        var totalSleep = Duration.ZERO
        val zone = queryTimeRange.from.zone
        var day = queryTimeRange.from.toLocalDate().minusDays(1)

        val sortedEvents = events.sortedBy { it.timeRange.from }

        var eventIndex = 0

        val minimalGap = Duration.ofHours(3)
        //checking night windows
        while (true) {
            if(eventIndex >= sortedEvents.size) break

            var maxGapDurationInWindow = Duration.ZERO
            val nightWindowStart = day.atTime(sleepWindowSettings.start).atZone(zone)
            val nightWindowEnd = day.plusDays(1).atTime(sleepWindowSettings.end).atZone(zone)

            if (nightWindowStart >= queryTimeRange.to) break

            val effectiveWindowStart = max(nightWindowStart, queryTimeRange.from)
            val effectiveWindowEnd = min(min(nightWindowEnd, queryTimeRange.to), now)

            if (effectiveWindowEnd > effectiveWindowStart) {
                while(eventIndex < sortedEvents.size - 1) {
                    val event1 = sortedEvents[eventIndex]
                    val event2 = sortedEvents[eventIndex + 1]

                    //before night window, continue until event2 starts in night window
                    if(event2.timeRange.from < effectiveWindowStart) {
                        eventIndex++
                        continue
                    }

                    //after night window, nothing to do
                    if (event1.timeRange.to > effectiveWindowEnd) break

                    //in night window, calculate gap between event1 and event2
                    val gapStart = max(event1.timeRange.to, effectiveWindowStart)
                    val gapEnd = min(event2.timeRange.from, effectiveWindowEnd)

                    val gap = Duration.between(gapStart, gapEnd)
                    if (gap > minimalGap && gap > maxGapDurationInWindow) {
                        maxGapDurationInWindow = gap
                    }

                    eventIndex++
                }

                if(maxGapDurationInWindow > Duration.ZERO) {
                    totalSleep = totalSleep.plus(maxGapDurationInWindow)
                } else {
                    totalSleep = totalSleep.plus(Duration.between(effectiveWindowStart, effectiveWindowEnd))
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
