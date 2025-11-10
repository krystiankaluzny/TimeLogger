package org.obywatelgcc.timelogger.statistics.components.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.BarAxisDrawer
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.BarDrawer
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.SimpleBarAxisDrawer
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.SimpleBarDrawer
import org.obywatelgcc.timelogger.statistics.components.chart.model.Data
import org.obywatelgcc.timelogger.statistics.components.chart.model.Scale

@Composable
fun <T> BarChart(
    modifier: Modifier = Modifier,
    data: List<Data<T>>,
    scale: Scale<T>,
    animation: AnimationSpec<Float> = TweenSpec<Float>(durationMillis = 3000),
    barHorizontalMargin: Dp = 3.dp
) {
    val transitionAnimation = remember(data) { Animatable(initialValue = 0f) }
    val rectangles = remember(data) { mutableStateMapOf<Data<T>, Rect>() }

    scale.adjust(data)

    val barDrawer = SimpleBarDrawer<T>(scale)
    val axisDrawer = SimpleBarAxisDrawer<T>(scale)

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
            axisDrawer.drawValueAxis(this, canvas, chartAreas.valueAxisArea)

            forEachWithArea(
                data,
                this,
                chartAreas.barDrawableArea,
                scale,
                transitionAnimation.value,
                barHorizontalMargin
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
        barDrawableArea = Rect(valueAxisRight, 0f, baseAxisRight, baseAxisTop)
    )
}

private fun <T> forEachWithArea(
    barsData: List<Data<T>>,
    drawScope: DrawScope,
    barDrawableArea: Rect,
    scale: Scale<T>,
    progress: Float,
    barHorizontalMargin: Dp,
    block: (barArea: Rect, data: Data<T>) -> Unit
) = with(drawScope) {
    val totalBars = barsData.size
    val widthOfBarArea = barDrawableArea.width / totalBars
    val barGapPx = barHorizontalMargin.toPx()

    barsData.forEachIndexed { index, data ->
        val barSegment =
            scale.scaleToSpaceSegment(data, Scale.SpaceSegment(barDrawableArea.top, barDrawableArea.bottom))

        val left = barDrawableArea.left + (index * widthOfBarArea)

        val barArea = Rect(
            left = left + barGapPx,
            right = left + widthOfBarArea - barGapPx,
            top = barDrawableArea.bottom - barSegment.to * progress,
            bottom = barDrawableArea.bottom - barSegment.from
        )
        block(barArea, data)
    }
}
