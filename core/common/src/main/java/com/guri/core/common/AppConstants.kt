package com.guri.core.common

/**
 * Application-wide constants
 */
object AppConstants {
    // Room related
    const val MAX_ROOM_PARTICIPANTS = 5
    const val ROOM_CODE_LENGTH = 9
    
    // Canvas related
    const val DEFAULT_STROKE_WIDTH = 5f
    
    // UI related
    val BRUSH_SIZES = listOf(2f, 5f, 10f, 15f, 20f)
    val DEFAULT_COLORS = listOf(
        0xFF000000, // Black
        0xFFFFFFFF, // White
        0xFFFF0000, // Red
        0xFF00FF00, // Green
        0xFF0000FF, // Blue
        0xFFFFFF00, // Yellow
        0xFF00FFFF, // Cyan
        0xFFFF00FF, // Magenta
        0xFFFFA500, // Orange
        0xFF800080  // Purple
    )
    
    // Firebase paths
    const val FIREBASE_USERS_PATH = "users"
    const val FIREBASE_ROOMS_PATH = "rooms"
    const val FIREBASE_PARTICIPANTS_PATH = "participants"
    const val FIREBASE_CANVAS_PATH = "canvas"
    const val FIREBASE_CURSORS_PATH = "cursors"
    const val FIREBASE_CHAT_PATH = "chat"
    const val FIREBASE_ACTIONS_PATH = "actions"
} 