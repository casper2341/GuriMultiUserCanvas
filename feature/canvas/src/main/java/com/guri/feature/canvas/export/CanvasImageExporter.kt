package com.guri.feature.canvas.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.guri.core.model.DrawPath

/**
 * Rasterizes a list of [DrawPath]s into a bitmap using the exact same per-[DrawPath.ToolType]
 * rules as [com.guri.core.ui.components.DrawingCanvas], but against [android.graphics.Canvas]
 * instead of Compose's DrawScope so it can run off the UI thread and doesn't depend on
 * composable-capture APIs unavailable at the project's pinned Compose BOM.
 */
object CanvasImageExporter {

    fun renderPathsToBitmap(paths: List<DrawPath>, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        paths.filter { !it.isDeleted }.forEach { path ->
            when (path.toolType) {
                DrawPath.ToolType.TEXT -> drawText(canvas, path)
                DrawPath.ToolType.SHAPE_LINE -> drawShapeLine(canvas, path, strokePaint)
                DrawPath.ToolType.SHAPE_RECTANGLE -> drawShapeRect(canvas, path, strokePaint, oval = false)
                DrawPath.ToolType.SHAPE_CIRCLE -> drawShapeRect(canvas, path, strokePaint, oval = true)
                else -> drawFreehand(canvas, path, strokePaint)
            }
        }

        return bitmap
    }

    private fun drawFreehand(canvas: Canvas, path: DrawPath, paint: Paint) {
        if (path.points.size < 2) return
        paint.color = if (path.toolType == DrawPath.ToolType.ERASER) Color.WHITE else path.color
        paint.alpha = if (path.toolType == DrawPath.ToolType.MARKER) 128 else 255
        paint.strokeWidth = path.strokeWidth

        val androidPath = android.graphics.Path().apply {
            moveTo(path.points.first().x, path.points.first().y)
            path.points.drop(1).forEach { point -> lineTo(point.x, point.y) }
        }
        canvas.drawPath(androidPath, paint)
    }

    private fun drawShapeLine(canvas: Canvas, path: DrawPath, paint: Paint) {
        if (path.points.size < 2) return
        paint.color = path.color
        paint.alpha = 255
        paint.strokeWidth = path.strokeWidth
        val start = path.points.first()
        val end = path.points.last()
        canvas.drawLine(start.x, start.y, end.x, end.y, paint)
    }

    private fun drawShapeRect(canvas: Canvas, path: DrawPath, paint: Paint, oval: Boolean) {
        if (path.points.size < 2) return
        paint.color = path.color
        paint.alpha = 255
        paint.strokeWidth = path.strokeWidth
        val start = path.points.first()
        val end = path.points.last()
        val rect = RectF(
            minOf(start.x, end.x),
            minOf(start.y, end.y),
            maxOf(start.x, end.x),
            maxOf(start.y, end.y)
        )
        if (oval) canvas.drawOval(rect, paint) else canvas.drawRect(rect, paint)
    }

    private fun drawText(canvas: Canvas, path: DrawPath) {
        val text = path.text
        val origin = path.points.firstOrNull()
        if (text.isNullOrEmpty() || origin == null) return
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = path.color
            textSize = path.strokeWidth * 4f
        }
        canvas.drawText(text, origin.x, origin.y, textPaint)
    }
}
