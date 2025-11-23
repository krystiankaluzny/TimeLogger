package org.obywatelgcc.timelogger.statistics.components.chart.drawer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.obywatelgcc.timelogger.statistics.components.chart.model.Scale

interface BarAxisDrawer<T> {
    fun baseAxisSafeSpace(drawScope: DrawScope): Float = 0f
    fun valueAxisSafeSpace(drawScope: DrawScope): Float = 0f

    fun drawBaseAxis(
        drawScope: DrawScope,
        canvas: Canvas,
        baseAxisArea: Rect
    )

    fun drawValueAxis(
        drawScope: DrawScope,
        canvas: Canvas,
        valueAxisArea: Rect,
        barDrawableArea: Rect
    )
}

class SimpleBarAxisDrawer<T>(
    private val scale: Scale<T>,
    private val axisLineThickness: Dp = 1.dp,
    private val axisLineColor: Color = Color.Black,
    private val labelTextSize: TextUnit = 12.sp,
    private val labelTextColor: Color = Color.Black,
) : BarAxisDrawer<T> {
    private val axisLinePaint = Paint().apply {
        isAntiAlias = true
        color = axisLineColor
        style = PaintingStyle.Stroke
    }
    private val gridLinePaint = Paint().apply {
        isAntiAlias = true
        color = axisLineColor.copy(alpha = 0.3f)
        style = PaintingStyle.Stroke
    }
    private val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = labelTextColor.toLegacyInt()
    }
    private val textBounds = android.graphics.Rect()

    override fun baseAxisSafeSpace(drawScope: DrawScope): Float = with(drawScope) {
        2f * axisLineThickness.toPx() + 5f * labelTextSize.toPx()
    }

    override fun valueAxisSafeSpace(drawScope: DrawScope): Float = with(drawScope) {
        return minOf(50.dp.toPx(), size.width * 10f / 100f)
    }

    override fun drawBaseAxis(drawScope: DrawScope, canvas: Canvas, baseAxisArea: Rect) = with(drawScope) {
        val lineThickness = axisLineThickness.toPx()
        val y = baseAxisArea.top + (lineThickness / 2f)

        canvas.drawLine(
            p1 = Offset(x = baseAxisArea.left, y = y),
            p2 = Offset(x = baseAxisArea.right, y = y),
            paint = axisLinePaint.apply {
                strokeWidth = lineThickness
            }
        )
    }

    override fun drawValueAxis(
        drawScope: DrawScope,
        canvas: Canvas,
        valueAxisArea: Rect,
        barDrawableArea: Rect
    ) {
        drawAxisLine(drawScope, canvas, valueAxisArea)
        drawAxisLabels(drawScope, canvas, valueAxisArea, barDrawableArea)
    }

    fun drawAxisLine(drawScope: DrawScope, canvas: Canvas, valueAxisArea: Rect) = with(drawScope) {
        val lineThickness = axisLineThickness.toPx()
        val x = valueAxisArea.right - (lineThickness / 2f)

        canvas.drawLine(
            p1 = Offset(x = x, y = valueAxisArea.top),
            p2 = Offset(x = x, y = valueAxisArea.bottom),
            paint = axisLinePaint.apply {
                strokeWidth = lineThickness
            }
        )
    }

    fun drawAxisLabels(drawScope: DrawScope, canvas: Canvas, valueAxisArea: Rect, barDrawableArea: Rect) =
        with(drawScope) {
            val lineThickness = axisLineThickness.toPx()
            val ticks = scale.ticksInSpaceSegment(Scale.SpaceSegment(valueAxisArea.bottom, valueAxisArea.top))

            val labelPaint = textPaint.apply {
                textSize = labelTextSize.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            ticks.forEach { tick ->
                val label = scale.valueToString(tick.value)
                labelPaint.getTextBounds(label, 0, label.length, textBounds)

                val tickX = valueAxisArea.right - axisLineThickness.toPx()
                val tickY = tick.spaceVale
                val textX = tickX - (labelTextSize.toPx() / 2f)
                val textY = tickY + (textBounds.height() / 2f)

                canvas.nativeCanvas.drawText(label, textX, textY, labelPaint)
                canvas.drawLine(
                    p1 = Offset(x = barDrawableArea.left, y = tickY),
                    p2 = Offset(x = barDrawableArea.right, y = tickY),
                    paint = gridLinePaint.apply {
                        strokeWidth = lineThickness
                    }
                )
            }
        }
}
