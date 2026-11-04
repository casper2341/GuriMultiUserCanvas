package com.guri.feature.canvas

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guri.core.common.AppConstants
import com.guri.core.data.repository.CanvasRepository
import com.guri.core.data.repository.RoomRepository
import com.guri.core.data.repository.UserRepository
import com.guri.core.model.CanvasState
import com.guri.core.model.ChatMessage
import com.guri.core.model.CursorPosition
import com.guri.core.model.DrawAction
import com.guri.core.model.DrawPath
import com.guri.core.model.DrawRoom
import com.guri.core.model.Participant
import com.guri.core.model.Result
import com.guri.feature.canvas.export.CanvasImageExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class CanvasUiState(
    val currentUserId: String = "",
    val currentUserName: String = "",
    val room: DrawRoom? = null,
    val participants: List<Participant> = emptyList(),
    val isAdmin: Boolean = false,
    val paths: List<DrawPath> = emptyList(),
    val isLocked: Boolean = false,
    val cursorPositions: List<CursorPosition> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val currentTool: DrawPath.ToolType = DrawPath.ToolType.PEN,
    val currentColor: Long = AppConstants.DEFAULT_COLORS[0],
    val currentStrokeWidth: Float = AppConstants.DEFAULT_STROKE_WIDTH,
    val pendingTextOffset: Offset? = null,
    val canvasSizePx: IntSize = IntSize.Zero,
    val isSavingImage: Boolean = false,
    val savedImageUrl: String? = null,
    val showClearConfirmation: Boolean = false,
    val errorMessage: String? = null,
    val wasKickedOrEnded: Boolean = false
)

private data class CanvasCombinedSnapshot(
    val canvasStateResult: Result<CanvasState>,
    val cursorsResult: Result<List<CursorPosition>>,
    val chatResult: Result<List<ChatMessage>>,
    val participantsResult: Result<List<Participant>>,
    val roomResult: Result<DrawRoom>
)

@HiltViewModel
class CanvasViewModel @Inject constructor(
    private val canvasRepository: CanvasRepository,
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: String = checkNotNull(savedStateHandle["roomId"])

    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState: StateFlow<CanvasUiState> = _uiState.asStateFlow()

    private var lastCursorSentAt = 0L
    private val cursorThrottleMs = 80L

    init {
        viewModelScope.launch {
            val user = (userRepository.getCurrentUser() as? Result.Success)?.data
            _uiState.value = _uiState.value.copy(
                currentUserId = user?.id.orEmpty(),
                currentUserName = user?.name.orEmpty()
            )

            combine(
                canvasRepository.observeCanvasState(roomId),
                canvasRepository.observeCursorPositions(roomId),
                canvasRepository.observeChatMessages(roomId),
                roomRepository.observeParticipants(roomId),
                roomRepository.observeRoom(roomId)
            ) { canvasStateResult, cursorsResult, chatResult, participantsResult, roomResult ->
                CanvasCombinedSnapshot(canvasStateResult, cursorsResult, chatResult, participantsResult, roomResult)
            }.collect { snapshot ->
                val canvasState = (snapshot.canvasStateResult as? Result.Success)?.data
                val cursors = (snapshot.cursorsResult as? Result.Success)?.data.orEmpty()
                val chat = (snapshot.chatResult as? Result.Success)?.data.orEmpty()
                val participants = (snapshot.participantsResult as? Result.Success)?.data.orEmpty()
                val room = (snapshot.roomResult as? Result.Success)?.data
                val userId = _uiState.value.currentUserId
                val stillPresent = participants.any { it.userId == userId }

                _uiState.value = _uiState.value.copy(
                    paths = canvasState?.paths.orEmpty(),
                    isLocked = canvasState?.isLocked ?: false,
                    cursorPositions = cursors.filter { it.userId != userId },
                    chatMessages = chat,
                    participants = participants,
                    room = room,
                    isAdmin = room?.creatorId == userId,
                    wasKickedOrEnded = room != null && ((participants.isNotEmpty() && !stillPresent) || !room.isActive)
                )
            }
        }
    }

    fun selectTool(tool: DrawPath.ToolType) {
        _uiState.value = _uiState.value.copy(currentTool = tool)
    }

    fun selectColor(color: Long) {
        _uiState.value = _uiState.value.copy(currentColor = color)
    }

    fun selectStrokeWidth(width: Float) {
        _uiState.value = _uiState.value.copy(currentStrokeWidth = width)
    }

    fun onPathDrawn(points: List<DrawPath.Point>) {
        val state = _uiState.value
        val path = DrawPath(
            roomId = roomId,
            userId = state.currentUserId,
            userName = state.currentUserName,
            color = state.currentColor.toInt(),
            strokeWidth = state.currentStrokeWidth,
            points = points,
            toolType = state.currentTool
        )
        addPathAndRecordAction(path)
    }

    fun onTextRequested(offset: Offset) {
        _uiState.value = _uiState.value.copy(pendingTextOffset = offset)
    }

    fun onTextCancelled() {
        _uiState.value = _uiState.value.copy(pendingTextOffset = null)
    }

    fun onTextConfirmed(text: String) {
        val offset = _uiState.value.pendingTextOffset
        _uiState.value = _uiState.value.copy(pendingTextOffset = null)
        if (offset == null || text.isBlank()) return

        val state = _uiState.value
        val path = DrawPath(
            roomId = roomId,
            userId = state.currentUserId,
            userName = state.currentUserName,
            color = state.currentColor.toInt(),
            strokeWidth = state.currentStrokeWidth,
            points = listOf(DrawPath.Point(offset.x, offset.y)),
            toolType = DrawPath.ToolType.TEXT,
            text = text
        )
        addPathAndRecordAction(path)
    }

    private fun addPathAndRecordAction(path: DrawPath) {
        viewModelScope.launch {
            when (val result = canvasRepository.addPath(path)) {
                is Result.Success -> canvasRepository.recordAction(
                    DrawAction(
                        roomId = roomId,
                        userId = path.userId,
                        pathId = result.data.id,
                        actionType = DrawAction.ActionType.ADD
                    )
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.exception.message)
                Result.Loading -> Unit
            }
        }
    }

    fun onCursorMoved(x: Float, y: Float) {
        val now = System.currentTimeMillis()
        if (now - lastCursorSentAt < cursorThrottleMs) return
        lastCursorSentAt = now

        val state = _uiState.value
        if (state.currentUserId.isBlank()) return
        viewModelScope.launch {
            canvasRepository.updateCursorPosition(
                CursorPosition(
                    roomId = roomId,
                    userId = state.currentUserId,
                    userName = state.currentUserName,
                    x = x,
                    y = y,
                    color = state.currentColor.toInt(),
                    visible = true
                )
            )
        }
    }

    fun undo() {
        val userId = _uiState.value.currentUserId
        if (userId.isBlank()) return
        viewModelScope.launch { canvasRepository.undoAction(roomId, userId) }
    }

    fun redo() {
        val userId = _uiState.value.currentUserId
        if (userId.isBlank()) return
        viewModelScope.launch { canvasRepository.redoAction(roomId, userId) }
    }

    fun requestClearCanvas() {
        _uiState.value = _uiState.value.copy(showClearConfirmation = true)
    }

    fun cancelClearCanvas() {
        _uiState.value = _uiState.value.copy(showClearConfirmation = false)
    }

    fun confirmClearCanvas() {
        _uiState.value = _uiState.value.copy(showClearConfirmation = false)
        viewModelScope.launch { canvasRepository.clearCanvas(roomId) }
    }

    fun toggleLock() {
        if (!_uiState.value.isAdmin) return
        val locked = _uiState.value.isLocked
        viewModelScope.launch { roomRepository.setRoomLocked(roomId, !locked) }
    }

    fun kick(participantId: String) {
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch { roomRepository.kickParticipant(roomId, participantId) }
    }

    fun leaveRoom() {
        val userId = _uiState.value.currentUserId
        if (userId.isBlank()) return
        viewModelScope.launch { roomRepository.leaveRoom(roomId, userId) }
    }

    fun sendChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val state = _uiState.value
        viewModelScope.launch {
            canvasRepository.addChatMessage(
                ChatMessage(
                    roomId = roomId,
                    userId = state.currentUserId,
                    userName = state.currentUserName,
                    message = trimmed
                )
            )
        }
    }

    fun onCanvasSizeChanged(size: IntSize) {
        _uiState.value = _uiState.value.copy(canvasSizePx = size)
    }

    fun saveCanvasImage() {
        val state = _uiState.value
        if (state.canvasSizePx.width <= 0 || state.canvasSizePx.height <= 0) return

        _uiState.value = state.copy(isSavingImage = true)
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.Default) {
                val bitmap = CanvasImageExporter.renderPathsToBitmap(
                    state.paths,
                    state.canvasSizePx.width,
                    state.canvasSizePx.height
                )
                ByteArrayOutputStream().use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.toByteArray()
                }
            }
            when (val result = canvasRepository.saveCanvasImage(roomId, bytes)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isSavingImage = false,
                    savedImageUrl = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSavingImage = false,
                    errorMessage = result.exception.message
                )
                Result.Loading -> _uiState.value = _uiState.value.copy(isSavingImage = false)
            }
        }
    }

    fun consumeSavedImageUrl() {
        _uiState.value = _uiState.value.copy(savedImageUrl = null)
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
