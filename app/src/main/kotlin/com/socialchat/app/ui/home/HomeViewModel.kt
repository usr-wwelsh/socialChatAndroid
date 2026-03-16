package com.socialchat.app.ui.home

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

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val selectedPost: Post? = null,
    val comments: List<Comment> = emptyList(),
    val commentsLoading: Boolean = false,
    val commentsError: String? = null,
    val commentInput: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val loadingMediaIds = mutableSetOf<Int>()

    init {
        loadFeed()
    }

    fun loadFeed(refresh: Boolean = false) {
        if (_uiState.value.isLoading) return
        val page = if (refresh) 1 else _uiState.value.currentPage

        viewModelScope.launch {
            _uiState.update {
                if (refresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = page == 1, error = null)
            }
            when (val result = repository.getFeed(page)) {
                is NetworkResult.Success -> {
                    val newPosts = result.data.posts ?: emptyList()
                    _uiState.update {
                        it.copy(
                            posts = if (refresh || page == 1) newPosts else it.posts + newPosts,
                            isLoading = false,
                            isRefreshing = false,
                            currentPage = page + 1,
                            hasMore = result.data.hasMore || newPosts.size >= 20
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun loadMoreIfNeeded(lastVisibleIndex: Int) {
        val state = _uiState.value
        if (!state.isLoading && state.hasMore && lastVisibleIndex >= state.posts.size - 3) {
            loadFeed()
        }
    }

    fun loadPostMedia(post: Post) {
        if (post.mediaData != null || post.mediaType == null) return
        if (loadingMediaIds.contains(post.id)) return
        loadingMediaIds.add(post.id)

        viewModelScope.launch {
            when (val result = repository.getPostMedia(post.id)) {
                is NetworkResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            posts = state.posts.map { p ->
                                if (p.id == post.id) p.copy(
                                    mediaData = result.data.mediaData,
                                    mediaType = result.data.mediaType
                                ) else p
                            }
                        )
                    }
                }
                else -> { loadingMediaIds.remove(post.id) }
            }
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            // Optimistic update
            _uiState.update { state ->
                state.copy(posts = state.posts.map { p ->
                    if (p.id == post.id) p.copy(
                        isLiked = !p.isLiked,
                        likeCount = if (p.isLiked) p.likeCount - 1 else p.likeCount + 1
                    ) else p
                })
            }
            repository.toggleLike(post.id, post.isLiked)
        }
    }

    fun openPostDetail(post: Post) {
        _uiState.update { it.copy(selectedPost = post, comments = post.previewComments ?: emptyList(), commentsError = null) }
        loadComments(post.id)
    }

    fun closePostDetail() {
        _uiState.update { it.copy(selectedPost = null, comments = emptyList(), commentInput = "", commentsError = null) }
    }

    private fun loadComments(postId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(commentsLoading = true, commentsError = null) }
            when (val result = repository.getComments(postId)) {
                is NetworkResult.Success -> _uiState.update { it.copy(comments = result.data, commentsLoading = false) }
                is NetworkResult.Error -> _uiState.update { state ->
                    state.copy(
                        commentsLoading = false,
                        commentsError = if (state.comments.isEmpty()) result.message else null
                    )
                }
                else -> {}
            }
        }
    }

    fun updateCommentInput(text: String) {
        _uiState.update { it.copy(commentInput = text) }
    }

    fun submitComment() {
        val state = _uiState.value
        val postId = state.selectedPost?.id ?: return
        val content = state.commentInput.trim()
        if (content.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(commentInput = "") }
            when (val result = repository.addComment(postId, content)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(comments = it.comments + result.data) }
                    // Update comment count in feed
                    _uiState.update { state2 ->
                        state2.copy(posts = state2.posts.map { p ->
                            if (p.id == postId) p.copy(commentCount = p.commentCount + 1) else p
                        })
                    }
                }
                else -> {}
            }
        }
    }
}
