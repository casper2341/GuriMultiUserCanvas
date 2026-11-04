package com.guri.core.model

/**
 * Data class representing a drawing path
 */
data class DrawPath(
    val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val userName: String = "",
    val color: Int = 0xFF000000.toInt(), // Default black color
    val strokeWidth: Float = 5f,
    val points: List<Point> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val toolType: ToolType = ToolType.PEN,
    val isDeleted: Boolean = false,
    val text: String? = null
) {
    /**
     * Data class representing a point in a drawing path
     */
    data class Point(
        val x: Float = 0f,
        val y: Float = 0f
    )
    
    /**
     * Enum representing the type of drawing tool
     */
    enum class ToolType {
        PEN,
        MARKER,
        ERASER,
        SHAPE_LINE,
        SHAPE_RECTANGLE,
        SHAPE_CIRCLE,
        TEXT
    }
} 