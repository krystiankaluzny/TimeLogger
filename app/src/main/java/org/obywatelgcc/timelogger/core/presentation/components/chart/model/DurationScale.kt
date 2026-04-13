package org.obywatelgcc.timelogger.core.presentation.components.chart.model

import java.time.Duration
import java.util.Locale
import kotlin.Double
import kotlin.text.toLong

class DurationScale(
    override var domain: Scale.ValueDomain<Duration> = DEFAULT_DOMAIN,
) : Scale<Duration> {

    companion object {
        private val EMPTY_DATA = Data(Duration.ZERO, "")
        private val DEFAULT_DOMAIN = Scale.ValueDomain(Duration.ZERO, Duration.ofHours(1L))
        private const val GAP_SCALE = 0.1

        fun minMaxDomain(
            data: List<Data<Duration>>,
            gapScale: Double = GAP_SCALE
        ): Scale.ValueDomain<Duration> {
            val min = data.minByOrNull { it.value } ?: EMPTY_DATA
            val max = data.maxByOrNull { it.value } ?: EMPTY_DATA

            if (min == EMPTY_DATA) {
                return DEFAULT_DOMAIN
            }

            val maxInMillis = max.value.toMillis()
            val minInMillis = min.value.toMillis()

            val gap = if(maxInMillis != minInMillis) maxInMillis - minInMillis else minInMillis

            return Scale.ValueDomain<Duration>(
                Duration.ofMillis(0.0.coerceAtLeast(minInMillis - gap * gapScale).toLong()),
                Duration.ofMillis((maxInMillis + gap * gapScale).toLong())
            )
        }

        fun totalDurationDomain(data: List<Data<Duration>>): Scale.ValueDomain<Duration> {
            val totalDuration = data
                .map { it.value }
                .fold(Duration.ZERO) { currentSum, duration -> currentSum.plus(duration) }

            return Scale.ValueDomain<Duration>(Duration.ZERO, totalDuration);
        }
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