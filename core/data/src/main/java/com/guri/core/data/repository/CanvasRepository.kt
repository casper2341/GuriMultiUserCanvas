package com.guri.core.data.repository

import com.guri.core.model.CanvasState
import com.guri.core.model.ChatMessage
import com.guri.core.model.CursorPosition
import com.guri.core.model.DrawAction
import com.guri.core.model.DrawPath
import com.guri.core.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository for canvas-related operations
 */
interface CanvasRepository {

    suspend fun addPath(path: DrawPath): Result<DrawPath>

    suspend fun removePath(pathId: String, roomId: String): Result<Unit>

    suspend fun clearCanvas(roomId: String): Result<Unit>

    suspend fun updateCursorPosition(position: CursorPosition): Result<Unit>

    fun observeCanvasState(roomId: String): Flow<Result<CanvasState>>

    fun observeCursorPositions(roomId: String): Flow<Result<List<CursorPosition>>>

    suspend fun addChatMessage(message: ChatMessage): Result<ChatMessage>

    fun observeChatMessages(roomId: String): Flow<Result<List<ChatMessage>>>

    suspend fun recordAction(action: DrawAction): Result<DrawAction>

    suspend fun undoAction(roomId: String, userId: String): Result<Unit>

    suspend fun redoAction(roomId: String, userId: String): Result<Unit>

    suspend fun saveCanvasImage(roomId: String, imageBytes: ByteArray): Result<String>
}
