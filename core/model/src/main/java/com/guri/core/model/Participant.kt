package com.guri.core.model

/**
 * Data class representing a participant in a drawing room
 */
data class Participant(
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val roomId: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false,
    val isActive: Boolean = true,
    val color: Int = -1 // The color assigned to the user in the room
) 