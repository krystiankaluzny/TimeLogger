package org.obywatelgcc.timelogger.core.presentation.components.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.bar.BarAxisDrawer
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.bar.BarDrawer
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.bar.SimpleBarAxisDrawer
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.bar.SimpleBarDrawer
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Data
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Scale
import java.lang.Math.clamp

@Composable
fun <T> BarChart(
    modifier: Modifier = Modifier,
    data: List<Data<T>>,
    scale: Scale<T>,
    barShowAnimation: AnimationSpec<Float> = TweenSpec<Float>(durationMillis = 1000),
    barTransitionAnimation: AnimationSpec<Float> = TweenSpec<Float>(
        durationMillis = 500,
        easing = LinearOutSlowInEasing
    ),
    properties: BarChartProperties = BarChartProperties()
) {
    var drawingResult by remember(data) { mutableStateOf(BarDrawer.DrawingResult.EMPTY) }
    val transitionAnimation = remember(data) { Animatable(initialValue = 0f) }
    val horizontalOffsetAnimation = remember(data) { Animatable(initialValue = 0.0f) }

    val barDrawer = SimpleBarDrawer<T>(
        scale = scale,
        labelTextSize = properties.labelTextSize,
        labelTextColor = properties.labelTextColor,
        valueTextSize = properties.barValueTextSize,
        valueTextColor = properties.barValueTextColor,
        valueDrawLocation =
            if (properties.barValueTextInsideBar) SimpleBarDrawer.ValueDrawLocation.Inside
            else SimpleBarDrawer.ValueDrawLocation.Outside,
        barMinWidth = properties.barMinWidth,
        barPadding = properties.barPadding
    )
    val axisDrawer = SimpleBarAxisDrawer<T>(
        scale = scale,
        axisLineThickness = properties.axisLineThickness,
        axisLineColor = properties.axisLineColor,
        labelTextSize = properties.tickerTextSize,
        labelTextColor = properties.tickerTextColor
    )

    LaunchedEffect(data) {
        transitionAnimation.animateTo(1f, animationSpec = barShowAnimation)
    }

    Canvas(
        modifier = modifier
            .padding()
            .fillMaxSize()
            .pointerInput(data) {
                coroutineScope {
                    detectTapGestures(
                        onDoubleTap = {
                            launch {
                                horizontalOffsetAnimation.animateTo(0f, animationSpec = barTransitionAnimation)
                            }
                        }
                    )
                }
            }
            .pointerInput(data) {
                coroutineScope {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            launch {
                                val offsetData = drawingResult.horizontalOffset
                                val offset = clamp(offsetData.current, offsetData.min, offsetData.max)
                                if(offset != offsetData.current) {
                                    horizontalOffsetAnimation.animateTo(offset, animationSpec = barTransitionAnimation)
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        launch {
                            horizontalOffsetAnimation.snapTo(drawingResult.horizontalOffset.current + dragAmount)
                        }
                    }
                }
            }
    ) {
        drawIntoCanvas { canvas ->
            val chartAreas = calculateChartAreas(this, barDrawer, axisDrawer)

            axisDrawer.drawBaseAxis(this, canvas, chartAreas.baseAxisArea)
            axisDrawer.drawValueAxis(this, canvas, chartAreas.valueAxisArea, chartAreas.barDrawableArea)

            val result = barDrawer.draw(
                this,
                canvas,
                data,
                chartAreas.barDrawableArea,
                transitionAnimation.value,
                horizontalOffsetAnimation.value
            )

            drawingResult = result
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
    val barMinWidth: Dp = 60.dp
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