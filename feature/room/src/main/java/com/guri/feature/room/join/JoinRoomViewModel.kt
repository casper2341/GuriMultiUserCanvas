package com.guri.feature.room.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guri.core.data.repository.RoomRepository
import com.guri.core.data.repository.UserRepository
import com.guri.core.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinRoomUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val joinedRoomId: String? = null
)

@HiltViewModel
class JoinRoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinRoomUiState())
    val uiState: StateFlow<JoinRoomUiState> = _uiState.asStateFlow()

    fun onCodeChanged(code: String) {
        _uiState.value = _uiState.value.copy(code = code.uppercase(), errorMessage = null)
    }

    fun joinRoom() {
        val code = _uiState.value.code.trim()
        if (code.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter a room code")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val userResult = userRepository.getCurrentUser()
            if (userResult !is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "You need to be signed in to join a room"
                )
                return@launch
            }
            val user = userResult.data

            val roomResult = roomRepository.getRoomByCode(code)
            if (roomResult !is Result.Success) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Room not found")
                return@launch
            }
            val room = roomResult.data

            when (val joinResult = roomRepository.joinRoom(room.id, user.id, user.name, user.photoUrl)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    joinedRoomId = room.id
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = joinResult.exception.message ?: "Failed to join room"
                )
                Result.Loading -> Unit
            }
        }
    }

    fun onNavigatedToRoom() {
        _uiState.value = _uiState.value.copy(joinedRoomId = null)
    }
}
