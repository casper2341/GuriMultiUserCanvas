package com.guri.core.network.service

import com.guri.core.model.Result
import com.guri.core.model.User
import kotlinx.coroutines.flow.Flow

interface UserNetworkService {
    
    /**
     * Get the current authenticated user
     */
    suspend fun getCurrentUser(): Result<User>
    
    /**
     * Sign in anonymously
     */
    suspend fun signInAnonymously(): Result<User>
    
    /**
     * Sign out the current user
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(user: User): Result<Unit>
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): Result<User>
    
    /**
     * Listen for user changes
     */
    fun observeCurrentUser(): Flow<Result<User>>
} 