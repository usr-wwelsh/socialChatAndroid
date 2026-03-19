package com.socialchat.app.ui.dm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialchat.app.core.crypto.CryptoManager
import com.socialchat.app.core.crypto.E2ECrypto
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.core.socket.SocketManager
import com.socialchat.app.data.model.DmConversation
import com.socialchat.app.data.model.DmMessage
import com.socialchat.app.data.model.User
import com.socialchat.app.data.repository.DmRepository
import com.socialchat.app.data.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DmListUiState(
    val conversations: List<DmConversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showFriendPicker: Boolean = false,
    val friends: List<User> = emptyList(),
    val friendsLoading: Boolean = false,
    val startingConversation: Boolean = false
)

data class DmConversationUiState(
    val messages: List<DmMessage> = emptyList(),
    val conversation: DmConversation? = null,
    val isLoading: Boolean = false,
    val input: String = "",
    val error: String? = null,
    val cryptoReady: Boolean = false,
    val needsPasswordUnlock: Boolean = false,
    val unlockError: String? = null,
    val isUnlocking: Boolean = false
)

@HiltViewModel
class DmViewModel @Inject constructor(
    private val repository: DmRepository,
    private val friendRepository: FriendRepository,
    private val socketManager: SocketManager,
    private val cryptoManager: CryptoManager,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _listState = MutableStateFlow(DmListUiState())
    val listState: StateFlow<DmListUiState> = _listState.asStateFlow()

    private val _convState = MutableStateFlow(DmConversationUiState())
    val convState: StateFlow<DmConversationUiState> = _convState.asStateFlow()

    private var currentConversationId: Int = -1
    private var currentUserId: Int = 0

    init {
        viewModelScope.launch { currentUserId = prefs.getUserId() ?: 0 }
        collectSocketEvents()
    }

    fun openFriendPicker() {
        _listState.update { it.copy(showFriendPicker = true, friends = emptyList(), friendsLoading = true) }
        viewModelScope.launch {
            val userId = prefs.getUserId() ?: return@launch
            when (val result = friendRepository.getUserFriends(userId)) {
                is NetworkResult.Success -> _listState.update {
                    it.copy(friends = result.data, friendsLoading = false)
                }
                is NetworkResult.Error -> _listState.update {
                    it.copy(friendsLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun closeFriendPicker() {
        _listState.update { it.copy(showFriendPicker = false) }
    }

    fun startConversationWith(friend: User, onConversationReady: (DmConversation) -> Unit) {
        _listState.update { it.copy(startingConversation = true, showFriendPicker = false) }
        viewModelScope.launch {
            when (val result = repository.getOrCreateConversation(friend.id)) {
                is NetworkResult.Success -> {
                    // Refresh list to get full conversation with partnerPublicKey
                    repository.invalidateConversations()
                    val convWithKey = when (val listResult = repository.getConversations(forceRefresh = true)) {
                        is NetworkResult.Success -> listResult.data.find { it.partnerId == friend.id }
                        else -> null
                    }
                    _listState.update { it.copy(startingConversation = false) }
                    val conversation = convWithKey ?: result.data.copy(
                        partnerUsername = friend.username,
                        partnerAvatar = friend.profilePicture
                    )
                    onConversationReady(conversation)
                }
                is NetworkResult.Error -> _listState.update {
                    it.copy(startingConversation = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun loadConversations(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _listState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getConversations(forceRefresh)) {
                is NetworkResult.Success -> _listState.update {
                    it.copy(conversations = result.data, isLoading = false)
                }
                is NetworkResult.Error -> _listState.update {
                    it.copy(error = result.message, isLoading = false)
                }
                else -> {}
            }
        }
    }

    fun openConversation(conversation: DmConversation) {
        if (currentConversationId != -1) socketManager.leaveDmConversation(currentConversationId)
        currentConversationId = conversation.id
        socketManager.joinDmConversation(conversation.id)

        viewModelScope.launch {
            val hasPrivateKey = cryptoManager.hasPrivateKey()
            val hasPartnerKey = !conversation.partnerPublicKey.isNullOrEmpty()
            _convState.value = DmConversationUiState(
                conversation = conversation,
                isLoading = true,
                cryptoReady = hasPrivateKey && hasPartnerKey,
                needsPasswordUnlock = !hasPrivateKey
            )
            when (val result = repository.getMessages(conversation.id, conversation.partnerPublicKey)) {
                is NetworkResult.Success -> _convState.update {
                    it.copy(messages = result.data, isLoading = false)
                }
                is NetworkResult.Error -> _convState.update {
                    it.copy(error = result.message, isLoading = false)
                }
                else -> {}
            }
        }
    }

    fun leaveConversation() {
        if (currentConversationId != -1) {
            socketManager.leaveDmConversation(currentConversationId)
            currentConversationId = -1
        }
        _convState.value = DmConversationUiState()
    }

    fun unlockWithPassword(password: String) {
        val conversation = _convState.value.conversation ?: return
        _convState.update { it.copy(isUnlocking = true, unlockError = null) }
        viewModelScope.launch {
            try {
                val userId = prefs.getUserId() ?: 0
                cryptoManager.initializeKeys(password, userId)
                val success = cryptoManager.hasPrivateKey()
                if (success) {
                    // Re-load messages now that keys are available
                    _convState.update { it.copy(needsPasswordUnlock = false, isUnlocking = false, cryptoReady = true) }
                    when (val result = repository.getMessages(conversation.id, conversation.partnerPublicKey, forceRefresh = true)) {
                        is NetworkResult.Success -> _convState.update { it.copy(messages = result.data) }
                        is NetworkResult.Error -> _convState.update { it.copy(error = result.message) }
                        else -> {}
                    }
                } else {
                    _convState.update { it.copy(isUnlocking = false, unlockError = "Wrong password or key unavailable") }
                }
            } catch (e: Exception) {
                _convState.update { it.copy(isUnlocking = false, unlockError = e.message ?: "Unlock failed") }
            }
        }
    }

    fun updateInput(text: String) {
        _convState.update { it.copy(input = text) }
    }

    fun sendMessage() {
        val state = _convState.value
        val content = state.input.trim()
        val conversation = state.conversation ?: return
        if (content.isBlank()) return

        _convState.update { it.copy(input = "", error = null) }

        viewModelScope.launch {
            when (val result = repository.sendMessage(
                conversation.id,
                conversation.partnerPublicKey,
                content
            )) {
                is NetworkResult.Error -> _convState.update { it.copy(error = result.message) }
                else -> {}
            }
        }
    }

    private fun collectSocketEvents() {
        viewModelScope.launch {
            socketManager.newDmMessages.collect { rawMsg ->
                // Decrypt and add to conversation if it's the current one
                if (rawMsg.conversationId == currentConversationId) {
                    val conversation = _convState.value.conversation
                    val sharedKey = if (conversation != null) {
                        cryptoManager.getSharedKey(rawMsg.conversationId, conversation.partnerPublicKey)
                    } else null

                    val decryptedContent = if (sharedKey != null) {
                        try {
                            E2ECrypto.decryptMessage(rawMsg.ciphertext, rawMsg.iv, sharedKey)
                        } catch (_: Exception) { "[encrypted]" }
                    } else "[encrypted]"

                    val decryptedMsg = rawMsg.copy(content = decryptedContent)
                    repository.appendCachedMessage(rawMsg.conversationId, decryptedMsg)
                    _convState.update { it.copy(messages = it.messages + decryptedMsg) }
                }
                // Refresh conversation list order
                repository.invalidateConversations()
            }
        }
        viewModelScope.launch {
            socketManager.dmNotifications.collect { (conversationId, _) ->
                repository.invalidateConversations()
                // Reload list if it's not the conversation we're in
                if (conversationId != currentConversationId) {
                    loadConversations(forceRefresh = true)
                }
            }
        }
    }
}
