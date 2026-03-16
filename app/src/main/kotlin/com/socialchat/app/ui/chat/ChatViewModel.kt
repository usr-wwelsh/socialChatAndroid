package com.socialchat.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.core.socket.SocketManager
import com.socialchat.app.data.model.ChatMessage
import com.socialchat.app.data.model.Chatroom
import com.socialchat.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatListUiState(
    val rooms: List<Chatroom> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatRoomUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val input: String = "",
    val typingUsers: Set<String> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val socketManager: SocketManager,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _listState = MutableStateFlow(ChatListUiState())
    val listState: StateFlow<ChatListUiState> = _listState.asStateFlow()

    private val _roomState = MutableStateFlow(ChatRoomUiState())
    val roomState: StateFlow<ChatRoomUiState> = _roomState.asStateFlow()

    private var currentRoomId: Int = -1
    private var stopTypingJob: Job? = null
    private var currentUsername: String = ""

    init {
        viewModelScope.launch {
            currentUsername = prefs.cachedUsername.first() ?: ""
        }
        collectSocketEvents()
    }

    fun loadRooms() {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true) }
            when (val result = repository.getRooms()) {
                is NetworkResult.Success -> _listState.update { it.copy(rooms = result.data, isLoading = false) }
                is NetworkResult.Error -> _listState.update { it.copy(error = result.message, isLoading = false) }
                else -> {}
            }
        }
    }

    fun joinRoom(roomId: Int) {
        if (currentRoomId != -1) socketManager.leaveRoom(currentRoomId)
        currentRoomId = roomId
        socketManager.joinRoom(roomId)
        loadMessages(roomId)
    }

    fun leaveRoom() {
        if (currentRoomId != -1) {
            socketManager.leaveRoom(currentRoomId)
            currentRoomId = -1
        }
        _roomState.value = ChatRoomUiState()
    }

    private fun loadMessages(roomId: Int) {
        viewModelScope.launch {
            _roomState.update { it.copy(isLoading = true) }
            when (val result = repository.getMessages(roomId)) {
                is NetworkResult.Success -> _roomState.update { it.copy(messages = result.data, isLoading = false) }
                is NetworkResult.Error -> _roomState.update { it.copy(error = result.message, isLoading = false) }
                else -> {}
            }
        }
    }

    fun updateInput(text: String) {
        _roomState.update { it.copy(input = text) }
        onTyping()
    }

    private fun onTyping() {
        if (currentRoomId == -1) return
        socketManager.sendTyping(currentRoomId)
        stopTypingJob?.cancel()
        stopTypingJob = viewModelScope.launch {
            delay(2000)
            socketManager.sendStopTyping(currentRoomId)
        }
    }

    fun sendMessage() {
        val state = _roomState.value
        val content = state.input.trim()
        if (content.isBlank() || currentRoomId == -1) return
        _roomState.update { it.copy(input = "") }
        stopTypingJob?.cancel()
        socketManager.sendStopTyping(currentRoomId)
        socketManager.sendMessage(currentRoomId, content)
    }

    private fun collectSocketEvents() {
        viewModelScope.launch {
            socketManager.newMessages.collect { msg ->
                if (msg.roomId == currentRoomId) {
                    _roomState.update { it.copy(messages = it.messages + msg) }
                }
            }
        }
        viewModelScope.launch {
            socketManager.typingEvents.collect { (roomId, username) ->
                if (roomId == currentRoomId && username != currentUsername) {
                    _roomState.update { it.copy(typingUsers = it.typingUsers + username) }
                }
            }
        }
        viewModelScope.launch {
            socketManager.stopTypingEvents.collect { (roomId, username) ->
                if (roomId == currentRoomId) {
                    _roomState.update { it.copy(typingUsers = it.typingUsers - username) }
                }
            }
        }
    }
}
