package com.socialchat.app.ui.profile

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.data.model.Post
import com.socialchat.app.data.model.User
import com.socialchat.app.data.repository.FriendRepository
import com.socialchat.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val friends: List<User> = emptyList(),
    val friendshipId: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val editBio: String = "",
    val editAvatarData: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var allPosts: List<Post> = emptyList()
    private val pageSize = 10

    private fun setAllPosts(posts: List<Post>) {
        allPosts = posts
        _uiState.update {
            it.copy(
                posts = posts.take(pageSize),
                currentPage = 1,
                hasMore = posts.size > pageSize
            )
        }
    }

    fun loadMorePosts() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return
        val nextPage = state.currentPage + 1
        val newDisplayed = allPosts.take(nextPage * pageSize)
        _uiState.update {
            it.copy(
                posts = newDisplayed,
                currentPage = nextPage,
                hasMore = newDisplayed.size < allPosts.size
            )
        }
    }

    fun loadMyProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = profileRepository.getMyProfile()) {
                is NetworkResult.Success -> {
                    val user = result.data.user
                    _uiState.update {
                        it.copy(
                            user = user,
                            isLoading = false,
                            editBio = user.bio ?: ""
                        )
                    }
                    setAllPosts(result.data.posts ?: emptyList())
                    // Load friends in background after profile is shown
                    when (val friendsResult = friendRepository.getUserFriends(user.id)) {
                        is NetworkResult.Success -> _uiState.update { it.copy(friends = friendsResult.data) }
                        else -> {}
                    }
                }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }

    fun loadUserProfile(username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = profileRepository.getUserProfile(username)) {
                is NetworkResult.Success -> {
                    val user = result.data.user
                    _uiState.update { it.copy(user = user, isLoading = false) }
                    setAllPosts(result.data.posts ?: emptyList())
                    // Load friends and friendship status in parallel
                    val friendsDeferred = async { friendRepository.getUserFriends(user.id) }
                    val statusDeferred = async { friendRepository.getFriendStatus(user.id) }
                    val friends = (friendsDeferred.await() as? NetworkResult.Success)?.data ?: emptyList()
                    val status = (statusDeferred.await() as? NetworkResult.Success)?.data
                    val updatedUser = user.copy(
                        isFriend = status?.status == "accepted",
                        friendRequestSent = status?.status == "pending" && status.isRequester == true
                    )
                    _uiState.update {
                        it.copy(
                            user = updatedUser,
                            friends = friends,
                            friendshipId = status?.friendshipId
                        )
                    }
                }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }

    fun updateEditBio(bio: String) = _uiState.update { it.copy(editBio = bio) }

    fun setEditAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                _uiState.update { it.copy(editAvatarData = "data:$mimeType;base64,$base64") }
            } catch (_: Exception) {}
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = profileRepository.updateProfile(state.editBio, state.editAvatarData)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(user = result.data, isUpdating = false, updateSuccess = true) }
                }
                is NetworkResult.Error -> _uiState.update { it.copy(isUpdating = false, error = result.message) }
                else -> {}
            }
        }
    }

    fun sendFriendRequest(userId: Int) {
        viewModelScope.launch {
            friendRepository.sendFriendRequest(userId)
            _uiState.update { it.copy(user = it.user?.copy(friendRequestSent = true)) }
        }
    }

    fun removeFriend() {
        val friendshipId = _uiState.value.friendshipId ?: return
        viewModelScope.launch {
            friendRepository.removeFriend(friendshipId)
            _uiState.update { it.copy(user = it.user?.copy(isFriend = false), friendshipId = null) }
        }
    }
}
