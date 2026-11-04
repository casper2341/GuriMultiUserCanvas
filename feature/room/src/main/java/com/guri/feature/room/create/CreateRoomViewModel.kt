package com.guri.feature.room.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guri.core.data.repository.RoomRepository
import com.guri.core.data.repository.UserRepository
import com.guri.core.model.DrawRoom
import com.guri.core.model.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateRoomUiState(
    val roomName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdRoomId: String? = null
)

@HiltViewModel
class CreateRoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRoomUiState())
    val uiState: StateFlow<CreateRoomUiState> = _uiState.asStateFlow()

    fun onRoomNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(roomName = name, errorMessage = null)
    }

    fun createRoom() {
        val name = _uiState.value.roomName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter a room name")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val userResult = userRepository.getCurrentUser()
            if (userResult !is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "You need to be signed in to create a room"
                )
                return@launch
            }
            val user = userResult.data

            val codeResult = roomRepository.generateRoomCode()
            if (codeResult !is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to generate a room code"
                )
                return@launch
            }

            when (
                val roomResult = roomRepository.createRoom(
                    DrawRoom(
                        code = codeResult.data,
                        name = name,
                        creatorId = user.id,
                        creatorName = user.name
                    )
                )
            ) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    createdRoomId = roomResult.data.id
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = roomResult.exception.message ?: "Failed to create room"
                )
                Result.Loading -> Unit
            }
        }
    }

    fun onNavigatedToRoom() {
        _uiState.value = _uiState.value.copy(createdRoomId = null)
    }
}
