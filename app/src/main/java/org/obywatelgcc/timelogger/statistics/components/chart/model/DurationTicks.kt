package org.obywatelgcc.timelogger.statistics.components.chart.model

import java.time.Duration
import java.time.temporal.ChronoUnit

private class TimeInterval(
    val units: ChronoUnit,
    private val floor: (Duration) -> Duration,
    private val offset: (Duration, Long) -> Duration,
    private val value: (Duration) -> Long
) {
    fun range(start: Duration, stop: Duration, step: Long): List<Duration> {
        val ticks = mutableListOf<Duration>()
        var time = floor(start)
        if (time < start) {
            time = offset(time, 1)
        }

        while (value(time) % step != 0L) {
            time = offset(time, 1)
        }

        while (time <= stop) {
            ticks.add(time)
            time = offset(time, step)
        }
        return ticks
    }
}

private val timeMillisecond = TimeInterval(
    units = ChronoUnit.MILLIS,
    floor = { it.truncatedTo(ChronoUnit.MILLIS) },
    offset = { date, step -> date.plusMillis(step) },
    value = { it.toMillis() }
)
private val timeSecond = TimeInterval(
    units = ChronoUnit.SECONDS,
    floor = { it.truncatedTo(ChronoUnit.SECONDS) },
    offset = { date, step -> date.plusSeconds(step) },
    value = { it.seconds }
)
private val timeMinute = TimeInterval(
    units = ChronoUnit.MINUTES,
    floor = { it.truncatedTo(ChronoUnit.MINUTES) },
    offset = { date, step -> date.plusMinutes(step) },
    value = { it.toMinutes() }
)
private val timeHour = TimeInterval(
    units = ChronoUnit.HOURS,
    floor = { it.truncatedTo(ChronoUnit.HOURS) },
    offset = { date, step -> date.plusHours(step) },
    value = { it.toHours() }
)
private val timeDay = TimeInterval(
    units = ChronoUnit.DAYS,
    floor = { it.truncatedTo(ChronoUnit.DAYS) },
    offset = { date, step -> date.plusDays(step) },
    value = { it.toDays() }
)

private val tickIntervals = listOf(
    Pair(timeMillisecond, 1L),
    Pair(timeMillisecond, 5L),
    Pair(timeMillisecond, 10L),
    Pair(timeMillisecond, 20L),
    Pair(timeMillisecond, 50L),
    Pair(timeMillisecond, 100L),
    Pair(timeMillisecond, 200L),
    Pair(timeMillisecond, 500L),
    Pair(timeSecond, 1L),
    Pair(timeSecond, 5L),
    Pair(timeSecond, 15L),
    Pair(timeSecond, 30L),
    Pair(timeMinute, 1L),
    Pair(timeMinute, 5L),
    Pair(timeMinute, 15L),
    Pair(timeMinute, 30L),
    Pair(timeHour, 1L),
    Pair(timeHour, 2L),
    Pair(timeHour, 3L),
    Pair(timeHour, 6L),
    Pair(timeHour, 12L),
    Pair(timeDay, 1L),
    Pair(timeDay, 2L)
    // Add more intervals as needed
)

private fun getIntervalDurationMillis(tickInterval: Pair<TimeInterval, Long>): Long {
    val durationInMs = tickInterval.first.units.duration.toMillis()
    val step = tickInterval.second
    return durationInMs * step
}

fun durationTicks(start: Duration, stop: Duration, count: Int): List<Duration> {
    if (count <= 0) return emptyList()

    val targetDuration = (stop.toMillis() - start.toMillis()) / count.toDouble()

    var foundIntervalIndex = tickIntervals.indexOfFirst {
        getIntervalDurationMillis(it) > targetDuration
    }
    if (foundIntervalIndex < 0) foundIntervalIndex = tickIntervals.lastIndex
    if (foundIntervalIndex > 0) {
        if (targetDuration / getIntervalDurationMillis(tickIntervals[foundIntervalIndex - 1])
            < getIntervalDurationMillis(tickIntervals[foundIntervalIndex]) / targetDuration
        ) {
            foundIntervalIndex--
        }
    }

    val bestIntervalData = tickIntervals[foundIntervalIndex]

    val (interval, step) = bestIntervalData

    val ticks = if (step > 0) interval.range(start, stop, step) else emptyList()
    return ticks
}