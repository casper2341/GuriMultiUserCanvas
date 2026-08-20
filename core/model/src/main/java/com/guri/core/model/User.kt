package com.guri.core.model

/**
 * Data class representing a user in the app
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
) 