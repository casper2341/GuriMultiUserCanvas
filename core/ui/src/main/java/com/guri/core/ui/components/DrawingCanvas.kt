package com.guri.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.guri.core.common.AppConstants
import com.guri.core.model.DrawPath

/**
 * A basic drawing canvas component that handles drawing with touch input
 */
@Composable
fun DrawingCanvas(
    paths: List<DrawPath> = emptyList(),
    currentColor: Long = AppConstants.DEFAULT_COLORS[0],
    currentStrokeWidth: Float = AppConstants.DEFAULT_STROKE_WIDTH,
    currentTool: DrawPath.ToolType = DrawPath.ToolType.PEN,
    onPathDrawn: (List<DrawPath.Point>) -> Unit = {},
    onCursorPositionChanged: (Float, Float) -> Unit = { _, _ -> },
    isLocked: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Track the current path being drawn
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPoints by remember { mutableStateOf<List<DrawPath.Point>>(emptyList()) }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(currentColor, currentStrokeWidth, currentTool, isLocked) {
                if (!isLocked) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = Path().apply {
                                moveTo(offset.x, offset.y)
                            }
                            currentPoints = listOf(DrawPath.Point(offset.x, offset.y))
                            onCursorPositionChanged(offset.x, offset.y)
                        },
                        onDrag = { change, dragAmount ->
                            val newPoint = change.position
                            currentPath?.lineTo(newPoint.x, newPoint.y)
                            currentPoints = currentPoints + DrawPath.Point(newPoint.x, newPoint.y)
                            onCursorPositionChanged(newPoint.x, newPoint.y)
                        },
                        onDragEnd = {
                            // Notify that a new path has been drawn
                            onPathDrawn(currentPoints)
                            
                            // Reset the current path
                            currentPath = null
                            currentPoints = emptyList()
                        }
                    )
                }
            }
    ) {
        // Draw all permanent paths
        paths.forEach { drawPath ->
            if (!drawPath.isDeleted && drawPath.points.size > 1) {
                val path = Path().apply {
                    moveTo(drawPath.points.first().x, drawPath.points.first().y)
                    drawPath.points.drop(1).forEach { point ->
                        lineTo(point.x, point.y)
                    }
                }
                
                // Draw the path with the appropriate style
                drawPath(
                    path = path,
                    color = Color(drawPath.color.toULong()),
                    style = Stroke(
                        width = drawPath.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
        
        // Draw the current path
        currentPath?.let { path ->
            drawPath(
                path = path,
                color = Color(currentColor.toULong()),
                style = Stroke(
                    width = currentStrokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrawingCanvasPreview() {
    MaterialTheme {
        DrawingCanvas()
    }
} 