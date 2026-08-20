package com.guri.core.model

/**
 * Data class representing a cursor position in the canvas
 */
data class CursorPosition(
    val roomId: String = "",
    val userId: String = "",
    val userName: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val color: Int = 0,
    val visible: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
) 