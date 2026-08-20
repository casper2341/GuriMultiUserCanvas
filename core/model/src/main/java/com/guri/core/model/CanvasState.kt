package com.guri.core.model

/**
 * Data class representing the state of the canvas
 */
data class CanvasState(
    val roomId: String = "",
    val paths: List<DrawPath> = emptyList(),
    val cursorPositions: Map<String, CursorPosition> = emptyMap(),
    val lastModified: Long = System.currentTimeMillis(),
    val isLocked: Boolean = false
) 