package com.guri.core.data.repository

import com.guri.core.model.DrawRoom
import com.guri.core.model.Participant
import com.guri.core.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository for room-related operations
 */
interface RoomRepository {

    suspend fun createRoom(room: DrawRoom): Result<DrawRoom>

    suspend fun getRoomById(roomId: String): Result<DrawRoom>

    suspend fun getRoomByCode(code: String): Result<DrawRoom>

    suspend fun joinRoom(roomId: String, userId: String, userName: String, photoUrl: String?): Result<Participant>

    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit>

    fun observeRoom(roomId: String): Flow<Result<DrawRoom>>

    fun observeParticipants(roomId: String): Flow<Result<List<Participant>>>

    suspend fun setRoomLocked(roomId: String, locked: Boolean): Result<Unit>

    suspend fun endRoom(roomId: String): Result<Unit>

    fun observeUserRooms(userId: String): Flow<Result<List<DrawRoom>>>

    suspend fun generateRoomCode(): Result<String>

    suspend fun kickParticipant(roomId: String, participantId: String): Result<Unit>
}
