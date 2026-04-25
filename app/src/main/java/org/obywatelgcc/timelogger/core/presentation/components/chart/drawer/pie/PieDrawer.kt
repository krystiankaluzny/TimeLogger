package org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.pie

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.bar.toLegacyInt
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.pie.DrawingElements.Gap
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.pie.DrawingElements.Hole
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.pie.DrawingElements.Label
import org.obywatelgcc.timelogger.core.presentation.components.chart.drawer.pie.DrawingElements.Slice
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Data
import org.obywatelgcc.timelogger.core.presentation.components.chart.model.Scale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

interface PieDrawer<T> {
    fun draw(
        drawScope: DrawScope,
        canvas: Canvas,
        data: List<Data<T>>,
        drawableArea: Rect,
        progress: Float
    )
}

class SimplePieDrawer<T>(
    private val scale: Scale<T>,
    private val holeRatio: Float,
    private val gapWidth: Float,
    private val backgroundColor: Color,
    private val labelTextSize: TextUnit = 14.sp,
    private val labelTextColor: Color = Black,
) : PieDrawer<T> {

    private val slicePaint = Paint().apply {
        this.isAntiAlias = true
    }

    private val labelPaint = android.graphics.Paint().apply {
        this.textAlign = android.graphics.Paint.Align.LEFT
        this.color = labelTextColor.toLegacyInt()
    }

    private val textBounds = android.graphics.Rect()

    override fun draw(
        drawScope: DrawScope,
        canvas: Canvas,
        data: List<Data<T>>,
        drawableArea: Rect,
        progress: Float
    ) {
        val input = DrawingInput(data, drawableArea, progress)
        val drawingElements = calculateDrawingElements(drawScope, input)

        draw(drawScope, canvas, drawingElements)
    }

    private fun calculateDrawingElements(
        drawScope: DrawScope,
        input: DrawingInput<T>
    ): DrawingElements = with(drawScope) {
        val (data, drawableArea, progress) = input

        val radius = min(drawableArea.width, drawableArea.height) / 2.0f
        val center = drawableArea.center
        val ovalRect = Rect(center, radius)

        val slices = mutableListOf<Slice>()
        val gaps = mutableListOf<Gap>()
        val labels = mutableListOf<Label>()
        val hasMultipleSlices = data.size > 1

        val holeRadius = radius * holeRatio
        val hole = Hole(center, holeRadius)

        val circleSpaceSegment = Scale.SpaceSegment(0.0f, 360.0f * progress)

        var prevSliceEndAngle = 0.0f

        val labelPaint = labelPaint.apply {
            textSize = labelTextSize.toPx()
        }

        for (slice in data) {
            val sliceSpaceSegment = scale.scaleToSpaceSegment(slice, circleSpaceSegment)

            val startAngle = prevSliceEndAngle
            val sweepAngle = sliceSpaceSegment.to

            prevSliceEndAngle += sweepAngle

            slices.add(
                Slice(
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    color = slice.color
                )
            )

            labels.add(
                createLabelElement(labelPaint, slice.label, startAngle, sweepAngle, radius, holeRadius, center)
            )

            if (hasMultipleSlices) {
                gaps.add(calculateGap(center, radius, boundaryAngle = startAngle))
            }
        }



        return DrawingElements(ovalRect, slices, gaps, labels, hole)
    }

    private fun createLabelElement(
        labelPaint: android.graphics.Paint,
        label: String,
        startAngle: Float,
        sweepAngle: Float,
        radius: Float,
        holeRadius: Float,
        center: Offset
    ): Label {

        labelPaint.getTextBounds(label, 0, label.length, textBounds)

        val labelAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
        val labelRadius = (radius - holeRadius) / 2 + holeRadius
        val labelX = (center.x + labelRadius * cos(labelAngle)).toFloat() - textBounds.width() / 2
        val labelY = (center.y + labelRadius * sin(labelAngle)).toFloat() + textBounds.height() / 2
        return Label(label, labelX, labelY)
    }

    /**
     * Builds a thin rectangular gap at the boundary between two slices.
     *
     * To place the rectangle without rotating the canvas, two vectors are used:
     *
     *   spine = ( cos(a),  sin(a) )  -- along the radius, toward the edge
     *   perp  = (-sin(a),  cos(a) )  -- sideways, perpendicular to the spine
     *
     * The four corners (w = gapWidth / 2):
     *
     *   A = center + perp *  w   (inner-left)
     *   B = center - perp *  w   (inner-right)
     *   C = edge   - perp *  w   (outer-right)
     *   D = edge   + perp *  w   (outer-left)
     *
     * where edge = center + spine * radius
     */
    private fun calculateGap(center: Offset, radius: Float, boundaryAngle: Float): Gap {
        val angle = Math.toRadians(boundaryAngle.toDouble())
        val cosA = cos(angle).toFloat()
        val sinA = sin(angle).toFloat()
        val halfW = gapWidth / 2f

        val centX = center.x
        val centY = center.y

        val aX = centX - sinA * halfW
        val aY = centY + cosA * halfW

        val bX = centX + sinA * halfW
        val bY = centY - cosA * halfW

        val cX = centX + radius * cosA + sinA * halfW
        val cY = centY + radius * sinA - cosA * halfW

        val dX = centX + radius * cosA - sinA * halfW
        val dY = centY + radius * sinA + cosA * halfW

        val path = Path().apply {
            moveTo(aX, aY)
            lineTo(bX, bY)
            lineTo(cX, cY)
            lineTo(dX, dY)
            close()
        }

        return Gap(path)
    }

    private fun draw(drawScope: DrawScope, canvas: Canvas, elements: DrawingElements) = with(drawScope) {
        elements.slices.forEach {
            canvas.drawArc(elements.ovalRect, it.startAngle, it.sweepAngle, true, slicePaint.apply { color = it.color })
        }

        elements.gaps.forEach { gap ->
            drawPath(
                path = gap.path,
                color = backgroundColor,
            )
        }

        val currentLabelPaint = labelPaint.apply {
            this.textSize = labelTextSize.toPx()
        }
        elements.labels.forEach {
            canvas.nativeCanvas.drawText(it.text, it.x, it.y, currentLabelPaint)
        }

        drawCircle(
            color = backgroundColor,
            radius = elements.hole.radius,
            center = elements.hole.center,
        )
    }

}

private data class DrawingInput<T>(
    val data: List<Data<T>>,
    val drawableArea: Rect,
    val progress: Float,
)

private data class DrawingElements(
    val ovalRect: Rect,
    val slices: List<Slice>,
    val gaps: List<Gap>,
    val labels: List<Label>,
    val hole: Hole,
) {
    data class Slice(
        val startAngle: Float,
        val sweepAngle: Float,
        val color: Color,
    )

    data class Gap(
        val path: Path,
    )

    data class Hole(
        val center: Offset,
        val radius: Float,
    )

    data class Label(
        val text: String,
        val x: Float,
        val y: Float,
    )
}
