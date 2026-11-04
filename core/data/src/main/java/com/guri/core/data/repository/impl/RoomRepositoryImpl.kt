package com.guri.core.data.repository.impl

import com.guri.core.data.repository.RoomRepository
import com.guri.core.model.DrawRoom
import com.guri.core.model.Participant
import com.guri.core.model.Result
import com.guri.core.network.service.RoomNetworkService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepositoryImpl @Inject constructor(
    private val roomNetworkService: RoomNetworkService
) : RoomRepository {

    override suspend fun createRoom(room: DrawRoom): Result<DrawRoom> {
        return roomNetworkService.createRoom(room)
    }

    override suspend fun getRoomById(roomId: String): Result<DrawRoom> {
        return roomNetworkService.getRoomById(roomId)
    }

    override suspend fun getRoomByCode(code: String): Result<DrawRoom> {
        return roomNetworkService.getRoomByCode(code)
    }

    override suspend fun joinRoom(
        roomId: String,
        userId: String,
        userName: String,
        photoUrl: String?
    ): Result<Participant> {
        return roomNetworkService.joinRoom(roomId, userId, userName, photoUrl)
    }

    override suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> {
        return roomNetworkService.leaveRoom(roomId, userId)
    }

    override fun observeRoom(roomId: String): Flow<Result<DrawRoom>> {
        return roomNetworkService.observeRoom(roomId)
    }

    override fun observeParticipants(roomId: String): Flow<Result<List<Participant>>> {
        return roomNetworkService.observeParticipants(roomId)
    }

    override suspend fun setRoomLocked(roomId: String, locked: Boolean): Result<Unit> {
        return roomNetworkService.setRoomLocked(roomId, locked)
    }

    override suspend fun endRoom(roomId: String): Result<Unit> {
        return roomNetworkService.endRoom(roomId)
    }

    override fun observeUserRooms(userId: String): Flow<Result<List<DrawRoom>>> {
        return roomNetworkService.observeUserRooms(userId)
    }

    override suspend fun generateRoomCode(): Result<String> {
        return roomNetworkService.generateRoomCode()
    }

    override suspend fun kickParticipant(roomId: String, participantId: String): Result<Unit> {
        return roomNetworkService.kickParticipant(roomId, participantId)
    }
}
