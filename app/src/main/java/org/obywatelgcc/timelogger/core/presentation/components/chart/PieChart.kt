package org.obywatelgcc.timelogger.core.presentation.components.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.MaterialTheme
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.pie.SimplePieDrawer
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Data
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Scale

data class PieChartProperties(
    val donutHoleRatio: Float = 0.4f,
    val gapWidth: Float = 20f,
    val animationDurationMs: Int = 800
)

@Composable
fun <T> PieChart(
    modifier: Modifier = Modifier,
    data: List<Data<T>>,
    scale: Scale<T>,
    properties: PieChartProperties = PieChartProperties()
) {
    if (data.isEmpty()) return

    val transitionAnimation = remember(data) { Animatable(initialValue = 0f) }
    val backgroundColor = MaterialTheme.colorScheme.background
    val pieDrawer = SimplePieDrawer(scale, properties.donutHoleRatio, properties.gapWidth, backgroundColor)

    LaunchedEffect(data) {
        transitionAnimation.animateTo(1f, animationSpec = TweenSpec<Float>(durationMillis = properties.animationDurationMs))
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {}
    ) {
        drawIntoCanvas { canvas ->
            val chartAreas = Rect(Offset(0f, 0f), size)
            pieDrawer.draw(this, canvas, data, chartAreas, transitionAnimation.value)
        }
    }
}
