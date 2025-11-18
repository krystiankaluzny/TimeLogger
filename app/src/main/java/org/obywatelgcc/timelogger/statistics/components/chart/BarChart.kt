package org.obywatelgcc.timelogger.statistics.components.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.BarAxisDrawer
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.BarDrawer
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.SimpleBarAxisDrawer
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.SimpleBarDrawer
import org.obywatelgcc.timelogger.statistics.components.chart.model.Data
import org.obywatelgcc.timelogger.statistics.components.chart.model.Scale
import kotlin.math.abs

@Composable
fun <T> BarChart(
    modifier: Modifier = Modifier,
    data: List<Data<T>>,
    scale: Scale<T>,
    animation: AnimationSpec<Float> = TweenSpec<Float>(durationMillis = 1000),
    properties: BarChartProperties = BarChartProperties()
) {
    val transitionAnimation = remember(data) { Animatable(initialValue = 0f) }
    val rectangles = remember(data) { mutableStateMapOf<Data<T>, Rect>() }

    scale.adjust(data)

    val barDrawer = SimpleBarDrawer<T>(
        scale = scale,
        labelTextSize = properties.labelTextSize,
        labelTextColor = properties.labelTextColor,
        valueTextSize = properties.barValueTextSize,
        valueTextColor = properties.barValueTextColor,
        valueDrawLocation =
            if (properties.barValueTextInsideBar) SimpleBarDrawer.ValueDrawLocation.Inside
            else SimpleBarDrawer.ValueDrawLocation.Outside
    )
    val axisDrawer = SimpleBarAxisDrawer<T>(
        scale = scale,
        axisLineThickness = properties.axisLineThickness,
        axisLineColor = properties.axisLineColor,
        labelTextSize = properties.tickerTextSize,
        labelTextColor = properties.tickerTextColor
    )

    LaunchedEffect(data) {
        transitionAnimation.animateTo(1f, animationSpec = animation)
    }

    Canvas(
        modifier = modifier
            .padding()
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures { offset ->
                    rectangles
                        .filter { it.value.contains(offset) }
//                        .forEach { it.key.onTap(it.key) }
                }
            }) {
        drawIntoCanvas { canvas ->
            val chartAreas = calculateChartAreas(this, barDrawer, axisDrawer)

            axisDrawer.drawBaseAxis(this, canvas, chartAreas.baseAxisArea)
            axisDrawer.drawValueAxis(this, canvas, chartAreas.valueAxisArea, chartAreas.barDrawableArea)

            forEachWithArea(
                data,
                this,
                chartAreas.barDrawableArea,
                scale,
                transitionAnimation.value,
                properties.barPadding
            ) { barArea, barData ->
                barDrawer.draw(this, canvas, barData, barArea)
                rectangles[barData] = barArea
            }
        }
    }
}

data class ChartAreas(
    val baseAxisArea: Rect,
    val valueAxisArea: Rect,
    val barDrawableArea: Rect
)

private fun <T> calculateChartAreas(
    drawScope: DrawScope,
    barDrawer: BarDrawer<T>,
    barAxisDrawer: BarAxisDrawer<T>
): ChartAreas {
    val chartSize = drawScope.size

    //We draw standard cartesian XY chart,
    //so baseAxis ia X-axis, and valueAxis i Y-axis
    //Be aware that screen coordinates are opposite to cartesian coordinate systems

    val valueAxisTop = barDrawer.valueSafeSpace(drawScope)
    val valueAxisRight = barAxisDrawer.valueAxisSafeSpace(drawScope)
    val baseAxisRight = chartSize.width
    val baseAxisTop = chartSize.height - barAxisDrawer.baseAxisSafeSpace(drawScope)

    return ChartAreas(
        baseAxisArea = Rect(valueAxisRight, baseAxisTop, baseAxisRight, chartSize.height),
        valueAxisArea = Rect(0f, valueAxisTop, valueAxisRight, baseAxisTop),
        barDrawableArea = Rect(valueAxisRight, valueAxisTop, baseAxisRight, baseAxisTop)
    )
}

private fun <T> forEachWithArea(
    barsData: List<Data<T>>,
    drawScope: DrawScope,
    barDrawableArea: Rect,
    scale: Scale<T>,
    progress: Float,
    barPadding: Dp,
    block: (barArea: Rect, data: Data<T>) -> Unit
) = with(drawScope) {
    val totalBars = barsData.size
    val widthOfBarArea = barDrawableArea.width / totalBars
    val barGapPx = barPadding.toPx()

    barsData.forEachIndexed { index, data ->
        val barSegment =
            scale.scaleToSpaceSegment(data, Scale.SpaceSegment(barDrawableArea.bottom, barDrawableArea.top))

        val barSegmentLength = abs(barSegment.to - barSegment.from)
        val left = barDrawableArea.left + (index * widthOfBarArea)

        val barArea = Rect(
            left = left + barGapPx,
            right = left + widthOfBarArea - barGapPx,
            top = barSegment.from - barSegmentLength * progress,
            bottom = barSegment.from
        )
        block(barArea, data)
    }
}

data class BarChartProperties(
    val axisLineThickness: Dp = 1.dp,
    val axisLineColor: Color = Black,

    val tickerTextSize: TextUnit = 12.sp,
    val tickerTextColor: Color = Black,

    val labelTextSize: TextUnit = 12.sp,
    val labelTextColor: Color = Black,

    val barValueTextSize: TextUnit = 14.sp,
    val barValueTextColor: Color = Black,
    val barValueTextInsideBar: Boolean = true,

    val barPadding: Dp = 3.dp,
    val barWidth: Dp = 100.dp
) {
    companion object {
        fun of(textColor: Color, axisLineColor: Color): BarChartProperties {
            return BarChartProperties(
                tickerTextColor = textColor,
                labelTextColor = textColor,
                barValueTextColor = textColor,
                axisLineColor = axisLineColor
            )
        }
    }
}