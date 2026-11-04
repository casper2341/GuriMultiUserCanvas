package com.guri.core.network.service.impl

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.guri.core.common.AppConstants
import com.guri.core.model.CanvasState
import com.guri.core.model.ChatMessage
import com.guri.core.model.CursorPosition
import com.guri.core.model.DrawAction
import com.guri.core.model.DrawPath
import com.guri.core.model.Result
import com.guri.core.network.service.CanvasNetworkService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanvasNetworkServiceImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) : CanvasNetworkService {

    private fun canvasRef(roomId: String) =
        database.getReference(AppConstants.FIREBASE_CANVAS_PATH).child(roomId)

    private fun pathsRef(roomId: String) = canvasRef(roomId).child("paths")

    private fun actionsRef(roomId: String) = canvasRef(roomId).child(AppConstants.FIREBASE_ACTIONS_PATH)

    private fun cursorsRef(roomId: String) =
        database.getReference(AppConstants.FIREBASE_CURSORS_PATH).child(roomId)

    private fun chatRef(roomId: String) =
        database.getReference(AppConstants.FIREBASE_CHAT_PATH).child(roomId)

    override suspend fun addPath(path: DrawPath): Result<DrawPath> {
        return try {
            val ref = if (path.id.isBlank()) pathsRef(path.roomId).push() else pathsRef(path.roomId).child(path.id)
            val finalPath = path.copy(id = ref.key ?: path.id)
            ref.setValue(finalPath).await()
            Result.success(finalPath)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun removePath(pathId: String, roomId: String): Result<Unit> {
        return try {
            pathsRef(roomId).child(pathId).child("isDeleted").setValue(true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun clearCanvas(roomId: String): Result<Unit> {
        return try {
            val snapshot = pathsRef(roomId).get().await()
            val updates = snapshot.children.mapNotNull { it.key }.associate { "$it/isDeleted" to true }
            if (updates.isNotEmpty()) {
                pathsRef(roomId).updateChildren(updates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun updateCursorPosition(position: CursorPosition): Result<Unit> {
        return try {
            cursorsRef(position.roomId).child(position.userId).setValue(position).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun observeCanvasState(roomId: String): Flow<Result<CanvasState>> = callbackFlow {
        val pathsMap = mutableMapOf<String, DrawPath>()
        var isLocked = false

        fun emitState() {
            trySend(
                Result.success(
                    CanvasState(
                        roomId = roomId,
                        paths = pathsMap.values.sortedBy { it.timestamp },
                        isLocked = isLocked,
                        lastModified = System.currentTimeMillis()
                    )
                )
            )
        }

        val childListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val key = snapshot.key ?: return
                snapshot.getValue(DrawPath::class.java)?.let { pathsMap[key] = it }
                emitState()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val key = snapshot.key ?: return
                snapshot.getValue(DrawPath::class.java)?.let { pathsMap[key] = it }
                emitState()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                pathsMap.remove(snapshot.key)
                emitState()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.error(error.toException()))
            }
        }
        pathsRef(roomId).addChildEventListener(childListener)

        val lockListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLocked = snapshot.getValue(Boolean::class.java) ?: false
                emitState()
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.error(error.toException()))
            }
        }
        canvasRef(roomId).child("isLocked").addValueEventListener(lockListener)

        awaitClose {
            pathsRef(roomId).removeEventListener(childListener)
            canvasRef(roomId).child("isLocked").removeEventListener(lockListener)
        }
    }

    override fun observeCursorPositions(roomId: String): Flow<Result<List<CursorPosition>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cursors = snapshot.children.mapNotNull { it.getValue(CursorPosition::class.java) }
                trySend(Result.success(cursors))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.error(error.toException()))
            }
        }
        val ref = cursorsRef(roomId)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun addChatMessage(message: ChatMessage): Result<ChatMessage> {
        return try {
            val ref = if (message.id.isBlank()) {
                chatRef(message.roomId).push()
            } else {
                chatRef(message.roomId).child(message.id)
            }
            val finalMessage = message.copy(id = ref.key ?: message.id)
            ref.setValue(finalMessage).await()
            Result.success(finalMessage)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override fun observeChatMessages(roomId: String): Flow<Result<List<ChatMessage>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children
                    .mapNotNull { it.getValue(ChatMessage::class.java) }
                    .sortedBy { it.timestamp }
                trySend(Result.success(messages))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.error(error.toException()))
            }
        }
        val ref = chatRef(roomId).limitToLast(200)
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun recordAction(action: DrawAction): Result<DrawAction> {
        return try {
            val ref = if (action.id.isBlank()) {
                actionsRef(action.roomId).push()
            } else {
                actionsRef(action.roomId).child(action.id)
            }
            val finalAction = action.copy(id = ref.key ?: action.id)
            ref.setValue(finalAction).await()
            Result.success(finalAction)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun undoAction(roomId: String, userId: String): Result<Unit> {
        return try {
            val snapshot = actionsRef(roomId).get().await()
            val actions = snapshot.children.mapNotNull { it.getValue(DrawAction::class.java) }
            val target = actions
                .filter { it.userId == userId && it.actionType == DrawAction.ActionType.ADD && !it.undone }
                .maxByOrNull { it.timestamp }
                ?: return Result.error(Exception("Nothing to undo"))

            pathsRef(roomId).child(target.pathId).child("isDeleted").setValue(true).await()
            actionsRef(roomId).child(target.id).child("undone").setValue(true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun redoAction(roomId: String, userId: String): Result<Unit> {
        return try {
            val snapshot = actionsRef(roomId).get().await()
            val actions = snapshot.children.mapNotNull { it.getValue(DrawAction::class.java) }
            val target = actions
                .filter { it.userId == userId && it.actionType == DrawAction.ActionType.ADD && it.undone }
                .maxByOrNull { it.timestamp }
                ?: return Result.error(Exception("Nothing to redo"))

            pathsRef(roomId).child(target.pathId).child("isDeleted").setValue(false).await()
            actionsRef(roomId).child(target.id).child("undone").setValue(false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun saveCanvasImage(roomId: String, imageBytes: ByteArray): Result<String> {
        return try {
            val ref = storage.reference
                .child(AppConstants.FIREBASE_CANVAS_PATH)
                .child(roomId)
                .child("${UUID.randomUUID()}.png")
            ref.putBytes(imageBytes).await()
            val url = ref.downloadUrl.await()
            Result.success(url.toString())
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}
