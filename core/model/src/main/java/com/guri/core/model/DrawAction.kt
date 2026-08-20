package com.guri.core.model

/**
 * Data class representing a drawing action (for undo/redo)
 */
data class DrawAction(
    val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val pathId: String = "",
    val actionType: ActionType = ActionType.ADD,
    val timestamp: Long = System.currentTimeMillis(),
    val undone: Boolean = false
) {
    /**
     * Enum representing the type of drawing action
     */
    enum class ActionType {
        ADD,
        REMOVE,
        CLEAR
    }
} 