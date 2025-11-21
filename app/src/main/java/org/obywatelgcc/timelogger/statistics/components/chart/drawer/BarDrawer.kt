package org.obywatelgcc.timelogger.statistics.components.chart.drawer


import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.BarDrawer.DrawingResult
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.BarDrawer.Offset
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.DrawingElements.Bar
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.DrawingElements.Text
import org.obywatelgcc.timelogger.statistics.components.chart.drawer.DrawingElements.VisibleBar
import org.obywatelgcc.timelogger.statistics.components.chart.model.Data
import org.obywatelgcc.timelogger.statistics.components.chart.model.Scale
import java.lang.Math.clamp
import kotlin.math.abs

interface BarDrawer<T> {
    fun valueSafeSpace(drawScope: DrawScope): Float = 0f

    fun draw(
        drawScope: DrawScope,
        canvas: Canvas,
        barsData: List<Data<T>>,
        barDrawableArea: Rect,
        progress: Float,
        horizontalOffset: Float
    ): DrawingResult

    data class DrawingResult(
        val horizontalOffset: Offset
    ) {
        companion object {
            val EMPTY = DrawingResult(Offset(0f, 0f, 0f, 0f, 0f))
        }
    }

    data class Offset(
        val min: Float,
        val max: Float,
        val suggestedMin: Float,
        val suggestedMax: Float,
        val current: Float
    )
}

class SimpleBarDrawer<T>(
    private val scale: Scale<T>,
    private val labelTextSize: TextUnit = 12.sp,
    private val labelTextColor: Color = Black,
    private val valueDrawLocation: ValueDrawLocation = ValueDrawLocation.Inside,
    private val valueTextSize: TextUnit = 14.sp,
    private val valueTextColor: Color = Black,
    private val barMinWidth: Dp = 100.dp,
    private val barPadding: Dp = 3.dp
) : BarDrawer<T> {
    private val barPaint = Paint().apply {
        this.isAntiAlias = true
    }

    private val labelPaint = android.graphics.Paint().apply {
        this.textAlign = android.graphics.Paint.Align.CENTER
        this.color = labelTextColor.toLegacyInt()
    }

    private val valuePaint = android.graphics.Paint().apply {
        this.textAlign = android.graphics.Paint.Align.CENTER
        this.color = valueTextColor.toLegacyInt()
    }

    override fun valueSafeSpace(drawScope: DrawScope): Float = when (valueDrawLocation) {
        ValueDrawLocation.Outside -> labelTextHeight(drawScope)
        ValueDrawLocation.Inside -> 0f
    }

    override fun draw(
        drawScope: DrawScope,
        canvas: Canvas,
        barsData: List<Data<T>>,
        barDrawableArea: Rect,
        progress: Float,
        horizontalOffset: Float
    ): DrawingResult {

        val input = DrawingInput(barsData, barDrawableArea, progress, horizontalOffset)
        val drawingElements = calculateDrawingElements(drawScope, input)

        draw(drawScope, canvas, drawingElements)

        return DrawingResult(drawingElements.horizontalOffset)
    }

    private fun calculateDrawingElements(drawScope: DrawScope, input: DrawingInput<T>): DrawingElements =
        with(drawScope) {

            val (barsData, barDrawableArea, progress, givenHorizontalOffset) = input
            val totalBars = barsData.size
            val widthOfBarArea = barMinWidth.toPx().coerceAtLeast(barDrawableArea.width / totalBars)
            val barGapPx = barPadding.toPx()

            val visibleBars = mutableListOf<VisibleBar>()

            val minOffset = barDrawableArea.width - (barsData.size * widthOfBarArea) - widthOfBarArea / 4
            val maxOffset = 0.0f

            val suggestedMin = minOffset - widthOfBarArea
            val suggestedMax = maxOffset + widthOfBarArea
            val horizontalOffset =
                clamp(
                    givenHorizontalOffset,
                    suggestedMin,
                    suggestedMax
                )

            val horizontalOffsetData = Offset(minOffset, maxOffset, suggestedMin, suggestedMax, horizontalOffset)

            barsData.forEachIndexed { index, data ->

                val barSegment =
                    scale.scaleToSpaceSegment(data, Scale.SpaceSegment(barDrawableArea.bottom, barDrawableArea.top))

                val barSegmentSize = abs(barSegment.to - barSegment.from)
                val left = barDrawableArea.left + (index * widthOfBarArea)

                val barArea = Rect(
                    left = left + barGapPx + horizontalOffset,
                    right = left + widthOfBarArea - barGapPx + horizontalOffset,
                    top = barSegment.from - barSegmentSize * progress,
                    bottom = barSegment.from
                )

                if (barArea.overlaps(barDrawableArea)) {
                    val barVisibleArea = barArea.intersect(barDrawableArea)

                    val xCenter = barArea.left + (barArea.width / 2)

                    val textOffsetFactor = if (index % 2 == 0) 1.0f else 2.0f
                    val yCenterLabel = barArea.bottom + textOffsetFactor * labelTextHeight(drawScope)

                    val labelElement = Text(data.label, xCenter, yCenterLabel)

                    val yCenterValue = when (valueDrawLocation) {
                        ValueDrawLocation.Inside -> (barArea.top + barArea.bottom) / 2
                        ValueDrawLocation.Outside -> (barArea.top) - valueTextSize.toPx() / 2
                    }

                    val valueStr = scale.valueToString(data.value)
                    val valueElement = Text(valueStr, xCenter, yCenterValue)

                    visibleBars.add(
                        VisibleBar(
                            bar = Bar(barVisibleArea, data.color),
                            label = labelElement,
                            value = valueElement
                        )
                    )
                }
            }

            return DrawingElements(visibleBars, horizontalOffsetData)
        }


    private fun draw(drawScope: DrawScope, canvas: Canvas, elements: DrawingElements) = with(drawScope) {

        elements.visibleBars.forEach {
            val area = it.bar.area
            val cornerRadius = 8f
            canvas.drawRoundRect(
                area.left,
                area.top,
                area.right,
                area.bottom,
                cornerRadius,
                cornerRadius,
                barPaint.apply { color = it.bar.color })

            val currentLabelPaint = labelPaint.apply { this.textSize = labelTextSize.toPx() }
            canvas.nativeCanvas.drawText(it.label.text, it.label.x, it.label.y, currentLabelPaint)

            val currentValuePaint = valuePaint.apply { this.textSize = valueTextSize.toPx() }
            canvas.nativeCanvas.drawText(it.value.text, it.value.x, it.value.y, currentValuePaint)
        }
    }

    private fun labelTextHeight(drawScope: DrawScope) = with(drawScope) {
        ((3f / 2f) * labelTextSize.toPx())
    }

    enum class ValueDrawLocation {
        Inside,
        Outside
    }
}

fun Color.toLegacyInt(): Int {
    return android.graphics.Color.argb(
        (alpha * 255.0f + 0.5f).toInt(),
        (red * 255.0f + 0.5f).toInt(),
        (green * 255.0f + 0.5f).toInt(),
        (blue * 255.0f + 0.5f).toInt()
    )
}

private data class DrawingInput<T>(
    val barsData: List<Data<T>>,
    val barDrawableArea: Rect,
    val progress: Float,
    val horizontalOffset: Float,
)

private data class DrawingElements(
    val visibleBars: List<VisibleBar>,
    val horizontalOffset: Offset
) {
    data class VisibleBar(
        val bar: Bar,
        val label: Text,
        val value: Text
    )

    data class Bar(
        val area: Rect,
        val color: Color
    )

    data class Text(
        val text: String,
        val x: Float,
        val y: Float
    )


}