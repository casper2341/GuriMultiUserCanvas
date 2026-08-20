package com.guri.core.model

/**
 * Data class representing a chat message
 */
data class ChatMessage(
    val id: String = "",
    val roomId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT
) {
    /**
     * Enum representing the type of message
     */
    enum class MessageType {
        TEXT,
        SYSTEM, // Messages from the system like "User X joined"
        VOICE  // For future voice message support
    }
} 