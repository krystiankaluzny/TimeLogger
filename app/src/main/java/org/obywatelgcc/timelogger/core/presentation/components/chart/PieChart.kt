package org.obywatelgcc.timelogger.core.presentation.components.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer

data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color
)

data class PieChartProperties(
    val donutHoleRatio: Float = 0.5f,
    val gapDegrees: Float = 2f,
    val animationDurationMs: Int = 800
)

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    slices: List<PieSlice>,
    properties: PieChartProperties = PieChartProperties()
) {
    if (slices.isEmpty()) return

    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return

    val sweepAnimation = remember(slices) { Animatable(0f) }
    LaunchedEffect(slices) {
        sweepAnimation.snapTo(0f)
        sweepAnimation.animateTo(
            targetValue = 1f,
            animationSpec = TweenSpec(durationMillis = properties.animationDurationMs)
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {}
    ) {
        val diameter = minOf(size.width, size.height) * 0.9f
        val radius = diameter / 2f
        val holeRadius = radius * properties.donutHoleRatio
        val topLeftX = (size.width - diameter) / 2f
        val topLeftY = (size.height - diameter) / 2f
        val progress = sweepAnimation.value

        var startAngle = -90f

        slices.forEach { slice ->
            val sweepAngle = (slice.value / total) * 360f * progress
            val effectiveSweep = (sweepAngle - properties.gapDegrees).coerceAtLeast(0f)

            drawArc(
                color = slice.color,
                startAngle = startAngle + properties.gapDegrees / 2f,
                sweepAngle = effectiveSweep,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(topLeftX, topLeftY),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )

            startAngle += sweepAngle
        }

        if (properties.donutHoleRatio > 0f) {
            drawCircle(
                color = Color.Transparent,
                radius = holeRadius,
                center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                blendMode = BlendMode.Clear
            )
        }
    }
}
