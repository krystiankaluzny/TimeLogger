package org.obywatelgcc.timelogger.statistics.presentation.stats

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import org.obywatelgcc.timelogger.core.data.DataStoreManager
import org.obywatelgcc.timelogger.core.data.minOf
import org.obywatelgcc.timelogger.core.data.maxOf
import org.obywatelgcc.timelogger.core.model.CalendarEvent
import org.obywatelgcc.timelogger.core.model.CalendarEventColor
import org.obywatelgcc.timelogger.core.model.ZonedDateTimeRange
import org.obywatelgcc.timelogger.core.presentation.BaseStateManager
import org.obywatelgcc.timelogger.settings.model.OtherSettings
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
        const val UNMEASURED_LABEL = "Unmeasured"
        val UNMEASURED_COLOR = CalendarEventColor("Unmeasured", "#FFFFB014", "", "", "")
        const val OTHERS_THRESHOLD = 40
    }

    fun recalculate(
        queryTimeRange: ZonedDateTimeRange,
        calendarEvents: List<CalendarEvent>,
        sleepWindowSettings: SleepWindowSettings,
        otherSettings: OtherSettings
    ) {
        val statisticItems = calendarEvents.groupingBy { it.title }
            .aggregate { key: String, acc: DataHolder?, event: CalendarEvent, first: Boolean ->

                val dataHolder = if (first || acc == null) {
                    DataHolder()
                } else acc

                //Cut event duration to query time range
                val eventDuration = Duration.between(
                    maxOf(event.timeRange.from, queryTimeRange.from),
                    minOf(event.timeRange.to, queryTimeRange.to)
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

        val sortedEvents = calendarEvents.sortedBy { it.timeRange.from }

        var sleepDuration = Duration.ZERO
        if (sleepWindowSettings.enabled) {
            sleepDuration = calculateSleepDuration(queryTimeRange, sortedEvents, sleepWindowSettings)
            if (!sleepDuration.isZero) {
                statisticItems.add(StatisticItem(SLEEP_LABEL, sleepDuration, SLEEP_COLOR))
            }
        }

        if (otherSettings.countUnmeasuredGaps) {
            val totalUnmeasuredDuration = calculateUnmeasuredDuration(queryTimeRange, sortedEvents)
            val unmeasuredDuration = totalUnmeasuredDuration - sleepDuration
            if (!unmeasuredDuration.isZero) {
                statisticItems.add(StatisticItem(UNMEASURED_LABEL, unmeasuredDuration, UNMEASURED_COLOR))
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
        sortedEvents: List<CalendarEvent>,
        sleepWindowSettings: SleepWindowSettings
    ): Duration {
        val now = ZonedDateTime.now()
        var totalSleep = Duration.ZERO
        val zone = queryTimeRange.from.zone
        var day = queryTimeRange.from.toLocalDate().minusDays(1)

        var eventIndex = 0

        val minimalGap = Duration.ofHours(3)
        //checking night windows
        while (true) {
            if(eventIndex >= sortedEvents.size) break

            var maxGapDurationInWindow = Duration.ZERO
            val nightWindowStart = day.atTime(sleepWindowSettings.start).atZone(zone)
            val nightWindowEnd = day.plusDays(1).atTime(sleepWindowSettings.end).atZone(zone)

            if (nightWindowStart >= queryTimeRange.to) break

            val effectiveWindowStart = maxOf(nightWindowStart, queryTimeRange.from)
            val effectiveWindowEnd = minOf(minOf(nightWindowEnd, queryTimeRange.to), now)

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
                    val gapStart = maxOf(event1.timeRange.to, effectiveWindowStart)
                    val gapEnd = minOf(event2.timeRange.from, effectiveWindowEnd)

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

    private fun calculateUnmeasuredDuration(
        queryTimeRange: ZonedDateTimeRange,
        sortedEvents: List<CalendarEvent>
    ): Duration {
        val now = ZonedDateTime.now()
        val effectiveEnd = minOf(queryTimeRange.to, now)
        if (!effectiveEnd.isAfter(queryTimeRange.from)) return Duration.ZERO

        var unmeasured = Duration.ZERO
        var previousEventEnd = queryTimeRange.from

        for (event in sortedEvents) {
            val eventStart = maxOf(event.timeRange.from, queryTimeRange.from)
            val eventEnd = minOf(event.timeRange.to, effectiveEnd)

            if(eventStart > effectiveEnd) break

            if (eventStart.isAfter(previousEventEnd)) {
                val gap = Duration.between(previousEventEnd, eventStart)
                logDebug("gap: $gap, event: ${event.title}")
                if (!gap.isNegative) unmeasured = unmeasured.plus(gap)
            }
            if (eventEnd.isAfter(previousEventEnd)) previousEventEnd = eventEnd
            if (!previousEventEnd.isBefore(effectiveEnd)) break
        }

        if (previousEventEnd.isBefore(effectiveEnd)) {
            logDebug("gap end: ${Duration.between(previousEventEnd, effectiveEnd)}")
            unmeasured = unmeasured.plus(Duration.between(previousEventEnd, effectiveEnd))
        }

        return unmeasured
    }
}

data class DataHolder(
    var totalDuration: Duration = Duration.ZERO,
    var color: CalendarEventColor? = null
)
