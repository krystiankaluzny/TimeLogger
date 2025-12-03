package org.obywatelgcc.timelogger.core.presentation.components.chart.model

class NumericScale<T : Number>(override var domain: Scale.ValueDomain<T>) : Scale<T> {
    override fun adjust(data: List<Data<T>>) {
        TODO("Not yet implemented")
    }

    override fun scaleToSpaceSegment(data: Data<T>, spaceSize: Scale.SpaceSegment): Scale.SpaceSegment {
        TODO("Not yet implemented")
    }

    override fun ticksInSpaceSegment(targetSpaceSegment: Scale.SpaceSegment): List<Scale.Tick<T>> {
        TODO("Not yet implemented")
    }
}