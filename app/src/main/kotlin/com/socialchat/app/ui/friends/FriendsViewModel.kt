package com.socialchat.app.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.data.model.Friendship
import com.socialchat.app.data.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsUiState(
    val requests: List<Friendship> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: FriendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init { loadRequests() }

    fun loadRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getFriendRequests()) {
                is NetworkResult.Success -> _uiState.update { it.copy(requests = result.data, isLoading = false) }
                is NetworkResult.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                else -> {}
            }
        }
    }

    fun acceptRequest(requestId: Int) {
        viewModelScope.launch {
            repository.acceptFriendRequest(requestId)
            _uiState.update { it.copy(requests = it.requests.filter { r -> r.id != requestId }) }
        }
    }

    fun rejectRequest(requestId: Int) {
        viewModelScope.launch {
            repository.rejectFriendRequest(requestId)
            _uiState.update { it.copy(requests = it.requests.filter { r -> r.id != requestId }) }
        }
    }
}
