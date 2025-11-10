package org.obywatelgcc.timelogger.statistics.components.chart.drawer


import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.obywatelgcc.timelogger.statistics.components.chart.model.Data
import org.obywatelgcc.timelogger.statistics.components.chart.model.Scale

interface BarDrawer<T> {
    fun valueSafeSpace(drawScope: DrawScope): Float = 0f

    fun draw(
        drawScope: DrawScope,
        canvas: Canvas,
        data: Data<T>,
        barArea: Rect
    )
}

class SimpleBarDrawer<T>(
    private val scale: Scale<T>,
    private val labelTextSize: TextUnit = 12.sp,
    private val labelTextColor: Color = Black,
    private val valueDrawLocation: ValueDrawLocation = ValueDrawLocation.Inside,
    private val valueTextSize: TextUnit = 14.sp,
    private val valueTextColor: Color = Black
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
        ValueDrawLocation.Outside -> (3f / 2f) * labelTextHeight(drawScope)
        ValueDrawLocation.Inside -> 0f
    }

    override fun draw(
        drawScope: DrawScope,
        canvas: Canvas,
        data: Data<T>,
        barArea: Rect
    ) {
        drawBar(canvas, barArea, data)
        drawLabel(drawScope, canvas, data, barArea)
        drawValue(drawScope, canvas, data, barArea)
    }

    private fun drawBar(canvas: Canvas, barArea: Rect, data: Data<T>) {
        canvas.drawRect(barArea, barPaint.apply {
            color = data.color
        })
    }

    private fun drawLabel(drawScope: DrawScope, canvas: Canvas, data: Data<T>, barArea: Rect) = with(drawScope) {
        val xCenter = barArea.left + (barArea.width / 2)
        val yCenter = barArea.bottom + labelTextHeight(drawScope)

        val currentLabelPaint = labelPaint.apply { this.textSize = labelTextSize.toPx() }
        canvas.nativeCanvas.drawText(data.label, xCenter, yCenter, currentLabelPaint)
    }

    fun drawValue(drawScope: DrawScope, canvas: Canvas, data: Data<T>, barArea: Rect) =
        with(drawScope) {
            val xCenter = barArea.left + (barArea.width / 2)

            val yCenter = when (valueDrawLocation) {
                ValueDrawLocation.Inside -> (barArea.top + barArea.bottom) / 2
                ValueDrawLocation.Outside -> (barArea.top) - valueTextSize.toPx() / 2
            }

            val currentValuePaint = valuePaint.apply { this.textSize = valueTextSize.toPx() }
            canvas.nativeCanvas.drawText(scale.valueToString(data.value), xCenter, yCenter, currentValuePaint)
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