package com.guri.core.network.service.impl

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.guri.core.common.AppConstants
import com.guri.core.model.DrawRoom
import com.guri.core.model.Participant
import com.guri.core.model.Result
import com.guri.core.network.service.RoomNetworkService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class RoomNetworkServiceImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val database: FirebaseDatabase
) : RoomNetworkService {

    private val roomsCollection = firestore.collection(AppConstants.FIREBASE_ROOMS_PATH)

    private fun participantsCollection(roomId: String) =
        roomsCollection.document(roomId).collection(AppConstants.FIREBASE_PARTICIPANTS_PATH)

    private fun canvasRef(roomId: String) =
        database.getReference(AppConstants.FIREBASE_CANVAS_PATH).child(roomId)

    override suspend fun createRoom(room: DrawRoom): Result<DrawRoom> {
        return try {
            val docRef = roomsCollection.document()
            val newRoom = room.copy(
                id = docRef.id,
                participantIds = listOf(room.creatorId),
                isActive = true,
                isLocked = false,
                createdAt = System.currentTimeMillis()
            )
            docRef.set(newRoom).await()

            val creatorParticipant = Participant(
                userId = room.creatorId,
                userName = room.creatorName,
                roomId = newRoom.id,
                isAdmin = true,
                isActive = true,
                color = AppConstants.DEFAULT_COLORS[0].toInt()
            )
            participantsCollection(newRoom.id).document(room.creatorId).set(creatorParticipant).await()

            canvasRef(newRoom.id).updateChildren(
                mapOf("creatorId" to room.creatorId, "isLocked" to false)
            ).await()

            Result.success(newRoom)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getRoomById(roomId: String): Result<DrawRoom> {
        return try {
            val doc = roomsCollection.document(roomId).get().await()
            val room = doc.toObject(DrawRoom::class.java)
            if (room != null) Result.success(room) else Result.error(Exception("Room not found"))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getRoomByCode(code: String): Result<DrawRoom> {
        return try {
            val snapshot = roomsCollection.whereEqualTo("code", code).limit(1).get().await()
            val room = snapshot.documents.firstOrNull()?.toObject(DrawRoom::class.java)
            if (room != null) Result.success(room) else Result.error(Exception("Room not found"))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun joinRoom(
        roomId: String,
        userId: String,
        userName: String,
        photoUrl: String?
    ): Result<Participant> {
        return try {
            val roomRef = roomsCollection.document(roomId)
            val participant = firestore.runTransaction { txn ->
                val snapshot = txn.get(roomRef)
                val room = snapshot.toObject(DrawRoom::class.java)
                    ?: throw Exception("Room not found")
                if (!room.isActive) throw Exception("Room has ended")

                if (userId in room.participantIds) {
                    return@runTransaction Participant(
                        userId = userId,
                        userName = userName,
                        userPhotoUrl = photoUrl,
                        roomId = roomId,
                        isAdmin = userId == room.creatorId,
                        isActive = true,
                        color = AppConstants.DEFAULT_COLORS[0].toInt()
                    )
                }
                if (room.isLocked) throw Exception("Room is locked")
                if (room.participantIds.size >= room.maxParticipants) throw Exception("Room is full")

                val color = AppConstants.DEFAULT_COLORS[
                    room.participantIds.size % AppConstants.DEFAULT_COLORS.size
                ].toInt()
                val newParticipant = Participant(
                    userId = userId,
                    userName = userName,
                    userPhotoUrl = photoUrl,
                    roomId = roomId,
                    isAdmin = false,
                    isActive = true,
                    color = color
                )
                txn.update(roomRef, "participantIds", FieldValue.arrayUnion(userId))
                txn.set(participantsCollection(roomId).document(userId), newParticipant)
                newParticipant
            }.await()

            Result.success(participant)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> {
        return try {
            val room = roomsCollection.document(roomId).get().await().toObject(DrawRoom::class.java)
            if (room != null && room.creatorId == userId) {
                return endRoom(roomId)
            }
            roomsCollection.document(roomId).update("participantIds", FieldValue.arrayRemove(userId)).await()
            participantsCollection(roomId).document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun observeRoom(roomId: String): Flow<Result<DrawRoom>> = callbackFlow {
        val listener = roomsCollection.document(roomId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.error(error))
                return@addSnapshotListener
            }
            val room = snapshot?.toObject(DrawRoom::class.java)
            if (room != null) {
                trySend(Result.success(room))
            } else {
                trySend(Result.error(Exception("Room not found")))
            }
        }
        awaitClose { listener.remove() }
    }

    override fun observeParticipants(roomId: String): Flow<Result<List<Participant>>> = callbackFlow {
        val listener = participantsCollection(roomId)
            .orderBy("joinedAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.error(error))
                    return@addSnapshotListener
                }
                val participants = snapshot?.documents
                    ?.mapNotNull { it.toObject(Participant::class.java) }
                    .orEmpty()
                trySend(Result.success(participants))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun setRoomLocked(roomId: String, locked: Boolean): Result<Unit> {
        return try {
            roomsCollection.document(roomId).update("isLocked", locked).await()
            canvasRef(roomId).child("isLocked").setValue(locked).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun endRoom(roomId: String): Result<Unit> {
        return try {
            roomsCollection.document(roomId).update(
                mapOf("isActive" to false, "isLocked" to true)
            ).await()
            canvasRef(roomId).child("isLocked").setValue(true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun observeUserRooms(userId: String): Flow<Result<List<DrawRoom>>> = callbackFlow {
        val listener = roomsCollection
            .whereEqualTo("creatorId", userId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.error(error))
                    return@addSnapshotListener
                }
                val rooms = snapshot?.documents
                    ?.mapNotNull { it.toObject(DrawRoom::class.java) }
                    .orEmpty()
                trySend(Result.success(rooms))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun generateRoomCode(): Result<String> {
        return try {
            val charset = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // excludes 0/O/1/I to avoid ambiguity
            repeat(5) {
                val code = (1..AppConstants.ROOM_CODE_LENGTH)
                    .map { charset[Random.nextInt(charset.length)] }
                    .joinToString("")
                val existing = roomsCollection.whereEqualTo("code", code).limit(1).get().await()
                if (existing.isEmpty) {
                    return Result.success(code)
                }
            }
            Result.error(Exception("Failed to generate a unique room code"))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun kickParticipant(roomId: String, participantId: String): Result<Unit> {
        return try {
            roomsCollection.document(roomId).update("participantIds", FieldValue.arrayRemove(participantId)).await()
            participantsCollection(roomId).document(participantId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}
