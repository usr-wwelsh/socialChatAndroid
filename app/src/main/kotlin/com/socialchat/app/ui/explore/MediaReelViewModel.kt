package com.socialchat.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.data.model.Comment
import com.socialchat.app.data.model.Post
import com.socialchat.app.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReelUiState(
    val posts: List<Post> = MediaReelCache.posts,
    val comments: List<Comment> = emptyList(),
    val isLoadingComments: Boolean = false,
    val commentsError: String? = null,
    val commentInput: String = ""
)

@HiltViewModel
class MediaReelViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReelUiState())
    val uiState: StateFlow<ReelUiState> = _uiState.asStateFlow()

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            val updated = post.copy(
                isLiked = !post.isLiked,
                likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
            )
            _uiState.update { it.copy(posts = it.posts.map { p -> if (p.id == post.id) updated else p }) }
            val result = postRepository.toggleLike(post.id, post.isLiked)
            if (result is NetworkResult.Error) {
                _uiState.update { it.copy(posts = it.posts.map { p -> if (p.id == post.id) post else p }) }
            }
        }
    }

    fun loadComments(postId: Int) {
        _uiState.update { it.copy(isLoadingComments = true, comments = emptyList(), commentsError = null) }
        viewModelScope.launch {
            when (val result = postRepository.getComments(postId)) {
                is NetworkResult.Success -> _uiState.update { it.copy(comments = result.data, isLoadingComments = false) }
                is NetworkResult.Error -> _uiState.update { it.copy(commentsError = result.message, isLoadingComments = false) }
                else -> _uiState.update { it.copy(isLoadingComments = false) }
            }
        }
    }

    fun onCommentInputChange(text: String) {
        _uiState.update { it.copy(commentInput = text) }
    }

    fun submitComment(postId: Int) {
        val content = _uiState.value.commentInput.trim()
        if (content.isEmpty()) return
        _uiState.update { it.copy(commentInput = "") }
        viewModelScope.launch {
            when (val result = postRepository.addComment(postId, content)) {
                is NetworkResult.Success -> _uiState.update { state ->
                    state.copy(
                        comments = state.comments + result.data,
                        posts = state.posts.map { p ->
                            if (p.id == postId) p.copy(commentCount = p.commentCount + 1) else p
                        }
                    )
                }
                else -> {}
            }
        }
    }
}
