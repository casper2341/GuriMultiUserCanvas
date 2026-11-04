package com.guri.core.data.repository.impl

import com.guri.core.data.repository.CanvasRepository
import com.guri.core.model.CanvasState
import com.guri.core.model.ChatMessage
import com.guri.core.model.CursorPosition
import com.guri.core.model.DrawAction
import com.guri.core.model.DrawPath
import com.guri.core.model.Result
import com.guri.core.network.service.CanvasNetworkService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanvasRepositoryImpl @Inject constructor(
    private val canvasNetworkService: CanvasNetworkService
) : CanvasRepository {

    override suspend fun addPath(path: DrawPath): Result<DrawPath> {
        return canvasNetworkService.addPath(path)
    }

    override suspend fun removePath(pathId: String, roomId: String): Result<Unit> {
        return canvasNetworkService.removePath(pathId, roomId)
    }

    override suspend fun clearCanvas(roomId: String): Result<Unit> {
        return canvasNetworkService.clearCanvas(roomId)
    }

    override suspend fun updateCursorPosition(position: CursorPosition): Result<Unit> {
        return canvasNetworkService.updateCursorPosition(position)
    }

    override fun observeCanvasState(roomId: String): Flow<Result<CanvasState>> {
        return canvasNetworkService.observeCanvasState(roomId)
    }

    override fun observeCursorPositions(roomId: String): Flow<Result<List<CursorPosition>>> {
        return canvasNetworkService.observeCursorPositions(roomId)
    }

    override suspend fun addChatMessage(message: ChatMessage): Result<ChatMessage> {
        return canvasNetworkService.addChatMessage(message)
    }

    override fun observeChatMessages(roomId: String): Flow<Result<List<ChatMessage>>> {
        return canvasNetworkService.observeChatMessages(roomId)
    }

    override suspend fun recordAction(action: DrawAction): Result<DrawAction> {
        return canvasNetworkService.recordAction(action)
    }

    override suspend fun undoAction(roomId: String, userId: String): Result<Unit> {
        return canvasNetworkService.undoAction(roomId, userId)
    }

    override suspend fun redoAction(roomId: String, userId: String): Result<Unit> {
        return canvasNetworkService.redoAction(roomId, userId)
    }

    override suspend fun saveCanvasImage(roomId: String, imageBytes: ByteArray): Result<String> {
        return canvasNetworkService.saveCanvasImage(roomId, imageBytes)
    }
}
