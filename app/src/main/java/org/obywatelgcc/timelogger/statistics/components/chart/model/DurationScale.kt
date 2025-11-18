package org.obywatelgcc.timelogger.statistics.components.chart.model

import java.time.Duration
import java.util.Locale

class DurationScale(
    override var domain: Scale.ValueDomain<Duration> = DEFAULT_DOMAIN
) : Scale<Duration> {

    companion object {
        private val EMPTY_DATA = Data(Duration.ZERO, "")
        private val DEFAULT_DOMAIN = Scale.ValueDomain(Duration.ZERO, Duration.ofHours(1L))
        private const val GAP_SCALE = 0.1
    }

    override fun adjust(data: List<Data<Duration>>) {
        if (domain != DEFAULT_DOMAIN) return

        val min = data.minByOrNull { it.value } ?: EMPTY_DATA
        val max = data.maxByOrNull { it.value } ?: EMPTY_DATA

        if (min == EMPTY_DATA) {
            domain = DEFAULT_DOMAIN
            return
        }

        val maxInMillis = max.value.toMillis()
        val minInMillis = min.value.toMillis()

        val gap = maxInMillis - minInMillis

        domain = Scale.ValueDomain<Duration>(
            Duration.ofMillis(0.0.coerceAtLeast(minInMillis - gap * GAP_SCALE).toLong()),
            Duration.ofMillis((maxInMillis + gap * GAP_SCALE).toLong())
        )
    }

    override fun valueToString(value: Duration): String {
        val durationSeconds = value.seconds
        val hoursPart = durationSeconds / 3600
        val minutesPart = (durationSeconds / 60) % 60
        val secondsPart = durationSeconds % 60

        return String.format(
            Locale.getDefault(Locale.Category.FORMAT),
            "%d:%02d:%02d",
            hoursPart,
            minutesPart,
            secondsPart
        )
    }

    override fun scaleToSpaceSegment(data: Data<Duration>, targetSpaceSegment: Scale.SpaceSegment): Scale.SpaceSegment {
        val dataValueMs = data.value.toMillis().toDouble()
        val zeroValueMs = 0.0

        val targetInverted = targetSpaceSegment.isInverted()
        val spaceSegment = targetSpaceSegment

        val (dataLocation, zeroLocation) = scaleValuesToSpaceSegment(
            spaceSegment,
            listOf(data.value, Duration.ZERO)
        )

        val result = if (zeroValueMs < dataValueMs) {
            if (targetInverted) {
                Scale.SpaceSegment(
                    from = maxOf(minOf(spaceSegment.from, zeroLocation), spaceSegment.to),
                    to = maxOf(minOf(spaceSegment.from, dataLocation), spaceSegment.to)
                )
            } else {
                Scale.SpaceSegment(
                    from = minOf(maxOf(spaceSegment.from, zeroLocation), spaceSegment.to),
                    to = minOf(maxOf(spaceSegment.from, dataLocation), spaceSegment.to)
                )
            }

        } else {
            if (targetInverted) {
                Scale.SpaceSegment(
                    from = maxOf(minOf(spaceSegment.from, dataLocation), spaceSegment.to),
                    to = maxOf(minOf(spaceSegment.from, zeroLocation), spaceSegment.to)
                )
            } else {
                Scale.SpaceSegment(
                    from = minOf(maxOf(spaceSegment.from, dataLocation), spaceSegment.to),
                    to = minOf(maxOf(spaceSegment.from, zeroLocation), spaceSegment.to)
                )
            }
        }

        return result
    }

    private fun scaleValuesToSpaceSegment(
        targetSpaceSegment: Scale.SpaceSegment,
        durations: List<Duration>
    ): List<Float> {
        val domainFromMs = domain.from.toMillis().toDouble()
        val domainToMs = domain.to.toMillis().toDouble()
        val domainRange = domainToMs - domainFromMs
        val spaceRange = targetSpaceSegment.to - targetSpaceSegment.from

        fun mapValueToSpace(valueMs: Double): Float {
            val ratio = (valueMs - domainFromMs) / domainRange
            return (targetSpaceSegment.from + ratio * spaceRange).toFloat()
        }

        return durations.map { mapValueToSpace(it.toMillis().toDouble()) }
    }

    override fun ticksInSpaceSegment(targetSpaceSegment: Scale.SpaceSegment): List<Scale.Tick<Duration>> {

        val durationTicks = durationTicks(domain.from, domain.to, 10)

        val spaceValues = scaleValuesToSpaceSegment(targetSpaceSegment, durationTicks)

        return durationTicks.zip(spaceValues).map {
            Scale.Tick(
                value = it.first,
                spaceVale = it.second
            )
        }
    }
}