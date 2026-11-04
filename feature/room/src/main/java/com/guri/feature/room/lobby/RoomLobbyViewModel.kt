package com.guri.feature.room.lobby

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guri.core.data.repository.RoomRepository
import com.guri.core.data.repository.UserRepository
import com.guri.core.model.DrawRoom
import com.guri.core.model.Participant
import com.guri.core.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomLobbyUiState(
    val room: DrawRoom? = null,
    val participants: List<Participant> = emptyList(),
    val currentUserId: String = "",
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
    val wasKicked: Boolean = false,
    val roomEnded: Boolean = false
)

@HiltViewModel
class RoomLobbyViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: String = checkNotNull(savedStateHandle["roomId"])

    private val _uiState = MutableStateFlow(RoomLobbyUiState())
    val uiState: StateFlow<RoomLobbyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val userResult = userRepository.getCurrentUser()
            val userId = (userResult as? Result.Success)?.data?.id.orEmpty()
            _uiState.value = _uiState.value.copy(currentUserId = userId)

            combine(
                roomRepository.observeRoom(roomId),
                roomRepository.observeParticipants(roomId)
            ) { roomResult, participantsResult ->
                roomResult to participantsResult
            }.collect { (roomResult, participantsResult) ->
                val room = (roomResult as? Result.Success)?.data
                val participants = (participantsResult as? Result.Success)?.data.orEmpty()
                val stillPresent = participants.any { it.userId == userId }

                _uiState.value = _uiState.value.copy(
                    room = room,
                    participants = participants,
                    isAdmin = room?.creatorId == userId,
                    isLoading = false,
                    wasKicked = room != null && participants.isNotEmpty() && !stillPresent,
                    roomEnded = room != null && !room.isActive
                )
            }
        }
    }

    fun toggleLock() {
        val room = _uiState.value.room ?: return
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch {
            roomRepository.setRoomLocked(room.id, !room.isLocked)
        }
    }

    fun kick(participantId: String) {
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch {
            roomRepository.kickParticipant(roomId, participantId)
        }
    }
}
