package com.guri.core.data.repository.impl

import com.guri.core.data.repository.UserRepository
import com.guri.core.model.Result
import com.guri.core.model.User
import com.guri.core.network.service.UserNetworkService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userNetworkService: UserNetworkService
) : UserRepository {
    
    override suspend fun getCurrentUser(): Result<User> {
        return userNetworkService.getCurrentUser()
    }
    
    override suspend fun signInAnonymously(): Result<User> {
        return userNetworkService.signInAnonymously()
    }
    
    override suspend fun signOut(): Result<Unit> {
        return userNetworkService.signOut()
    }
    
    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return userNetworkService.updateUserProfile(user)
    }
    
    override suspend fun getUserById(userId: String): Result<User> {
        return userNetworkService.getUserById(userId)
    }
    
    override fun observeCurrentUser(): Flow<Result<User>> {
        return userNetworkService.observeCurrentUser()
    }
} 