package com.guri.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.guri.core.common.AppConstants
import com.guri.core.model.DrawPath
import kotlin.math.abs
import kotlin.math.min

/**
 * A drawing canvas component that handles drawing with touch input, branching its
 * render/gesture behavior on [DrawPath.ToolType].
 */
@Composable
fun DrawingCanvas(
    paths: List<DrawPath> = emptyList(),
    currentColor: Long = AppConstants.DEFAULT_COLORS[0],
    currentStrokeWidth: Float = AppConstants.DEFAULT_STROKE_WIDTH,
    currentTool: DrawPath.ToolType = DrawPath.ToolType.PEN,
    onPathDrawn: (List<DrawPath.Point>) -> Unit = {},
    onCursorPositionChanged: (Float, Float) -> Unit = { _, _ -> },
    onTextRequested: (Offset) -> Unit = {},
    isLocked: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Track the points of the stroke currently being drawn (freehand) or the shape's
    // start/drag points (for SHAPE_* tools, only the first and last are kept on commit).
    var currentPoints by remember { mutableStateOf<List<DrawPath.Point>>(emptyList()) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(currentColor, currentStrokeWidth, currentTool, isLocked) {
                if (isLocked) return@pointerInput
                if (currentTool == DrawPath.ToolType.TEXT) {
                    detectTapGestures(
                        onTap = { offset -> onTextRequested(offset) }
                    )
                } else {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints = listOf(DrawPath.Point(offset.x, offset.y))
                            onCursorPositionChanged(offset.x, offset.y)
                        },
                        onDrag = { change, _ ->
                            val newPoint = change.position
                            currentPoints = currentPoints + DrawPath.Point(newPoint.x, newPoint.y)
                            onCursorPositionChanged(newPoint.x, newPoint.y)
                        },
                        onDragEnd = {
                            val finalPoints = if (isShapeTool(currentTool) && currentPoints.size >= 2) {
                                listOf(currentPoints.first(), currentPoints.last())
                            } else {
                                currentPoints
                            }
                            if (finalPoints.isNotEmpty()) {
                                onPathDrawn(finalPoints)
                            }
                            currentPoints = emptyList()
                        }
                    )
                }
            }
    ) {
        // Draw all permanent paths
        paths.forEach { drawPath ->
            if (!drawPath.isDeleted) {
                drawStoredPath(drawPath)
            }
        }

        // Draw the in-progress path/shape preview
        if (currentPoints.size > 1) {
            val previewPoints = if (isShapeTool(currentTool)) {
                listOf(currentPoints.first(), currentPoints.last())
            } else {
                currentPoints
            }
            drawStroke(
                points = previewPoints,
                color = resolveStrokeColor(Color(currentColor), currentTool),
                strokeWidth = currentStrokeWidth,
                toolType = currentTool
            )
        }
    }
}

private fun isShapeTool(tool: DrawPath.ToolType): Boolean =
    tool == DrawPath.ToolType.SHAPE_LINE ||
        tool == DrawPath.ToolType.SHAPE_RECTANGLE ||
        tool == DrawPath.ToolType.SHAPE_CIRCLE

/** ERASER always paints in the canvas background color; MARKER is semi-transparent. */
private fun resolveStrokeColor(baseColor: Color, toolType: DrawPath.ToolType): Color =
    when (toolType) {
        DrawPath.ToolType.ERASER -> Color.White
        DrawPath.ToolType.MARKER -> baseColor.copy(alpha = 0.5f)
        else -> baseColor
    }

private fun DrawScope.drawStoredPath(drawPath: DrawPath) {
    if (drawPath.toolType == DrawPath.ToolType.TEXT) {
        drawTextPath(drawPath)
        return
    }
    if (drawPath.points.size < 2) return
    drawStroke(
        points = drawPath.points,
        color = resolveStrokeColor(Color(drawPath.color), drawPath.toolType),
        strokeWidth = drawPath.strokeWidth,
        toolType = drawPath.toolType
    )
}

private fun DrawScope.drawStroke(
    points: List<DrawPath.Point>,
    color: Color,
    strokeWidth: Float,
    toolType: DrawPath.ToolType
) {
    if (points.size < 2) return
    val start = points.first()
    val end = points.last()

    when (toolType) {
        DrawPath.ToolType.SHAPE_LINE -> {
            drawLine(
                color = color,
                start = Offset(start.x, start.y),
                end = Offset(end.x, end.y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        DrawPath.ToolType.SHAPE_RECTANGLE, DrawPath.ToolType.SHAPE_CIRCLE -> {
            val topLeft = Offset(min(start.x, end.x), min(start.y, end.y))
            val size = Size(abs(end.x - start.x), abs(end.y - start.y))
            if (toolType == DrawPath.ToolType.SHAPE_RECTANGLE) {
                drawRect(color = color, topLeft = topLeft, size = size, style = Stroke(width = strokeWidth))
            } else {
                drawOval(color = color, topLeft = topLeft, size = size, style = Stroke(width = strokeWidth))
            }
        }

        else -> {
            // PEN, MARKER, ERASER: freehand polyline through every recorded point.
            val path = Path().apply {
                moveTo(start.x, start.y)
                points.drop(1).forEach { point -> lineTo(point.x, point.y) }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

private fun DrawScope.drawTextPath(drawPath: DrawPath) {
    val origin = drawPath.points.firstOrNull() ?: return
    val text = drawPath.text
    if (text.isNullOrEmpty()) return
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = drawPath.color
            textSize = drawPath.strokeWidth * 4f
            isAntiAlias = true
        }
        canvas.nativeCanvas.drawText(text, origin.x, origin.y, paint)
    }
}

@Preview(showBackground = true)
@Composable
fun DrawingCanvasPreview() {
    MaterialTheme {
        DrawingCanvas()
    }
}
