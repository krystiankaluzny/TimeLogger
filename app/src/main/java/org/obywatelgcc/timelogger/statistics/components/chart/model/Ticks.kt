package org.obywatelgcc.timelogger.statistics.components.chart.model

import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

private val E10 = sqrt(50.0) // This corresponds to a step of 5 (sqrt(5*10))
private val E5 = sqrt(10.0)  // This corresponds to a step of 2 (sqrt(2*5))
private val E2 = sqrt(2.0)   // This corresponds to a step of 1 (sqrt(1*2))

/**
 * Calculates the increment ("step") for axis ticks, aiming for "nice" rounded values.
 * An equivalent of `tickStep` from d3-array.
 *
 * @param start The lower bound of the range.
 * @param stop The upper bound of the range.
 * @param count The suggested number of ticks/steps.
 * @return The calculated, "nice" step increment.
 */
fun tickStep(start: Double, stop: Double, count: Int): Double {
    val step0 = (stop - start) / count.coerceAtLeast(1)
    val power = floor(log10(step0))
    val error = step0 / 10.0.pow(power)

    val baseValue = when {
        error >= E10 -> 10.0
        error >= E5 -> 5.0
        error >= E2 -> 2.0
        else -> 1.0
    }

    return when {
        power >= 0 -> baseValue * 10.0.pow(power)
        else -> -(10.0.pow(-power) / baseValue)
    }
}

/**
 * Generates an array of "nice", rounded values (ticks) within a given range.
 * An equivalent of `ticks` from d3-array.
 *
 * @param start The lower bound of the range.
 * @param stop The upper bound of the range.
 * @param count The suggested number of ticks to generate.
 * @return A list of Double values representing the axis ticks.
 */
fun ticks(start: Double, stop: Double, count: Int): List<Double> {
    if (count <= 0) return emptyList()

    var s = start
    var e = stop

    // If stop < start, swap them and reverse the result at the end
    val reverse = e < s
    if (reverse) {
        val temp = s
        s = e
        e = temp
    }

    val step = tickStep(s, e, count)

    // If the step is zero or infinite (e.g., start == stop), return a list with one element
    if (step == 0.0 || !step.isFinite()) {
        return if (s.isFinite()) listOf(s) else emptyList()
    }

    val result = mutableListOf<Double>()
    val n = round(s / step)
    var i = n

    // Use a simple tolerance comparison to avoid floating-point precision issues
    val tolerance = 1e-9

    // Loop to generate ticks
    while (i * step <= e + tolerance) {
        val tickValue = i * step
        // Add only ticks that are actually within the [start, stop] range
        if (tickValue >= s - tolerance && tickValue <= e + tolerance) {
            result.add(tickValue)
        }
        i++
    }

    return if (reverse) result.reversed() else result
}