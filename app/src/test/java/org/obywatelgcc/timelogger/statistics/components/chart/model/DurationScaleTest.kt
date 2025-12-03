package org.obywatelgcc.timelogger.statistics.components.chart.model

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.core.test.TestScope
import io.kotest.matchers.shouldBe
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Data
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.DurationScale
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Scale
import java.time.Duration

class DurationScaleTest : ShouldSpec({
    context("adjust") {
        should("calculate domain with padding") {
            //given
            val initialData = listOf(dataOfMinutes(110), dataOfMinutes(40))
            val scale = DurationScale()

            //when
            scale.adjust(initialData)

            //then
            val expectedPadding = 7L // (110 - 40) / 10
            scale.domain.from shouldBe Duration.ofMinutes(40L - expectedPadding)
            scale.domain.to shouldBe Duration.ofMinutes(110L + expectedPadding)
        }

        should("calculate domain with padding but not less than 0") {
            //given
            val initialData = listOf(dataOfMinutes(1010), dataOfMinutes(10))
            val scale = DurationScale()

            //when
            scale.adjust(initialData)

            //then
            val expectedPadding = 100L // (1010 - 10) / 10
            scale.domain.from shouldBe Duration.ofMinutes(0)
            scale.domain.to shouldBe Duration.ofMinutes(1010L + expectedPadding)
        }
    }

    context("ticksInSpaceSegment") {
        should("calculate ticks") {
            //given
            val from = 40L
            val to = 110L
            val initialData = listOf(dataOfMinutes(to), dataOfMinutes(from))
            val scale = DurationScale()
            val segment = Scale.SpaceSegment(100f, 1000f)
            scale.adjust(initialData)

            //when
            val ticks = scale.ticksInSpaceSegment(segment)

            //then
            ticks.size shouldBe 17 //35 to 115

            val expectedTickValues = 35L.rangeTo(115L).step(5).map { Duration.ofMinutes(it.toLong()) }

            ticks.map { it.value } shouldBe expectedTickValues
        }
    }
})

private fun TestScope.dataOfMinutes(valueMin: Long): Data<Duration> {
    return Data(Duration.ofMinutes(valueMin), "")
}
