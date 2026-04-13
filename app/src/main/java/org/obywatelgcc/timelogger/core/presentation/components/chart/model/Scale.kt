package org.obywatelgcc.timelogger.core.presentation.components.chart.model

interface Scale<T> {
    var domain: ValueDomain<T>

    fun valueToString(value: T): String = value.toString()

    fun scaleToSpaceSegment(data: Data<T>, targetSpaceSegment: SpaceSegment) : SpaceSegment

    fun ticksInSpaceSegment(targetSpaceSegment: SpaceSegment): List<Tick<T>>

    data class ValueDomain<T>(
        val from: T,
        val to: T
    )

    data class SpaceSegment(
        val from: Float,
        val to: Float
    ) {
        fun isInverted(): Boolean = from > to
        fun invert(): SpaceSegment = SpaceSegment(to, from)
    }

    data class Tick<T>(
        val value: T,
        val spaceVale: Float
    )
}

