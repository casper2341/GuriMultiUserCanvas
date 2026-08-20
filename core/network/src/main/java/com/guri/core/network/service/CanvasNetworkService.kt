package com.guri.core.network.service

import com.guri.core.model.CanvasState
import com.guri.core.model.ChatMessage
import com.guri.core.model.CursorPosition
import com.guri.core.model.DrawAction
import com.guri.core.model.DrawPath
import com.guri.core.model.Result
import kotlinx.coroutines.flow.Flow

interface CanvasNetworkService {
    
    /**
     * Add a path to the canvas
     */
    suspend fun addPath(path: DrawPath): Result<DrawPath>
    
    /**
     * Remove a path from the canvas
     */
    suspend fun removePath(pathId: String, roomId: String): Result<Unit>
    
    /**
     * Clear all paths from the canvas
     */
    suspend fun clearCanvas(roomId: String): Result<Unit>
    
    /**
     * Update cursor position
     */
    suspend fun updateCursorPosition(position: CursorPosition): Result<Unit>
    
    /**
     * Observe canvas state
     */
    fun observeCanvasState(roomId: String): Flow<Result<CanvasState>>
    
    /**
     * Observe cursor positions
     */
    fun observeCursorPositions(roomId: String): Flow<Result<List<CursorPosition>>>
    
    /**
     * Add a chat message
     */
    suspend fun addChatMessage(message: ChatMessage): Result<ChatMessage>
    
    /**
     * Observe chat messages
     */
    fun observeChatMessages(roomId: String): Flow<Result<List<ChatMessage>>>
    
    /**
     * Record an action for undo/redo
     */
    suspend fun recordAction(action: DrawAction): Result<DrawAction>
    
    /**
     * Undo the last action
     */
    suspend fun undoAction(roomId: String, userId: String): Result<Unit>
    
    /**
     * Redo the last undone action
     */
    suspend fun redoAction(roomId: String, userId: String): Result<Unit>
    
    /**
     * Save canvas as image
     */
    suspend fun saveCanvasImage(roomId: String, imageBytes: ByteArray): Result<String>
} 