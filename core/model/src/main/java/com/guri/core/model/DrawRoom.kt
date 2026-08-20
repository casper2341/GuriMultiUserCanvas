package com.guri.core.model

/**
 * Data class representing a drawing room
 */
data class DrawRoom(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val isActive: Boolean = true,
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val participantIds: List<String> = emptyList(),
    val maxParticipants: Int = 5
) 