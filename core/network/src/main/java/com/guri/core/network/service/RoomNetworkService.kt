package com.guri.core.network.service

import com.guri.core.model.DrawRoom
import com.guri.core.model.Participant
import com.guri.core.model.Result
import kotlinx.coroutines.flow.Flow

interface RoomNetworkService {
    
    /**
     * Create a new drawing room
     */
    suspend fun createRoom(room: DrawRoom): Result<DrawRoom>
    
    /**
     * Get room by ID
     */
    suspend fun getRoomById(roomId: String): Result<DrawRoom>
    
    /**
     * Get room by code
     */
    suspend fun getRoomByCode(code: String): Result<DrawRoom>
    
    /**
     * Join a room
     */
    suspend fun joinRoom(roomId: String, userId: String, userName: String, photoUrl: String?): Result<Participant>
    
    /**
     * Leave a room
     */
    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit>
    
    /**
     * Observe a room
     */
    fun observeRoom(roomId: String): Flow<Result<DrawRoom>>
    
    /**
     * Observe participants in a room
     */
    fun observeParticipants(roomId: String): Flow<Result<List<Participant>>>
    
    /**
     * Lock/unlock a room
     */
    suspend fun setRoomLocked(roomId: String, locked: Boolean): Result<Unit>
    
    /**
     * End a room session
     */
    suspend fun endRoom(roomId: String): Result<Unit>
    
    /**
     * Get active rooms created by user
     */
    fun observeUserRooms(userId: String): Flow<Result<List<DrawRoom>>>
    
    /**
     * Generate a unique room code
     */
    suspend fun generateRoomCode(): Result<String>
    
    /**
     * Kick a participant from a room
     */
    suspend fun kickParticipant(roomId: String, participantId: String): Result<Unit>
} 