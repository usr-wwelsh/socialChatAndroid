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
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

data class FeedState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val currentPage: Int = 1
)

data class PostDetailState(
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

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _feedState = MutableStateFlow(FeedState())
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()

    private val _detailState = MutableStateFlow(PostDetailState())
    val detailState: StateFlow<PostDetailState> = _detailState.asStateFlow()

    private val loadingMediaIds = mutableSetOf<Int>()

    init {
        loadFeed()
    }

    fun loadFeed(refresh: Boolean = false) {
        if (_feedState.value.isLoading) return
        val page = if (refresh) 1 else _feedState.value.currentPage

        viewModelScope.launch {
            _feedState.update {
                if (refresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = page == 1, error = null)
            }
            when (val result = repository.getFeed(page, refresh = refresh)) {
                is NetworkResult.Success -> {
                    val newPosts = result.data.posts ?: emptyList()
                    _posts.update { current ->
                        if (refresh || page == 1) newPosts else current + newPosts
                    }
                    _feedState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            currentPage = page + 1,
                            hasMore = result.data.hasMore || newPosts.size >= 20
                        )
                    }
                    batchLoadMedia(newPosts)
                }
                is NetworkResult.Error -> {
                    _feedState.update { it.copy(isLoading = false, isRefreshing = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    private fun batchLoadMedia(newPosts: List<Post>) {
        val postsNeedingMedia = newPosts.filter { it.mediaType != null && it.mediaUrl == null }
        if (postsNeedingMedia.isEmpty()) return
        val semaphore = Semaphore(5)
        viewModelScope.launch {
            postsNeedingMedia.forEach { post ->
                launch {
                    semaphore.withPermit { loadPostMedia(post) }
                }
            }
        }
    }

    fun loadMoreIfNeeded(lastVisibleIndex: Int) {
        val state = _feedState.value
        if (!state.isLoading && state.hasMore && lastVisibleIndex >= _posts.value.size - 3) {
            loadFeed()
        }
    }

    private suspend fun loadPostMedia(post: Post) {
        if (post.mediaUrl != null || post.mediaType == null) return
        if (loadingMediaIds.contains(post.id)) return
        loadingMediaIds.add(post.id)

        when (val result = repository.getPostMedia(post.id)) {
            is NetworkResult.Success -> {
                val mediaUrl = result.data.mediaUrl
                val mediaType = result.data.mediaType
                _posts.update { posts ->
                    posts.map { p ->
                        if (p.id == post.id) p.copy(mediaUrl = mediaUrl, mediaType = mediaType) else p
                    }
                }
                repository.updateCachedPost(post.id) { p ->
                    p.copy(mediaUrl = mediaUrl, mediaType = mediaType)
                }
            }
            else -> loadingMediaIds.remove(post.id)
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            val updated = post.copy(
                isLiked = !post.isLiked,
                likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
            )
            _posts.update { posts -> posts.map { if (it.id == post.id) updated else it } }
            _detailState.update { state ->
                if (state.selectedPost?.id == post.id) state.copy(selectedPost = updated) else state
            }
            repository.toggleLike(post.id, post.isLiked)
        }
    }

    fun openPostDetail(post: Post) {
        _detailState.update {
            it.copy(selectedPost = post, comments = post.previewComments ?: emptyList(), commentsError = null)
        }
        loadComments(post.id)
    }

    fun closePostDetail() {
        _detailState.update {
            it.copy(selectedPost = null, comments = emptyList(), commentInput = "", commentsError = null)
        }
    }

    private fun loadComments(postId: Int) {
        viewModelScope.launch {
            _detailState.update { it.copy(commentsLoading = true, commentsError = null) }
            when (val result = repository.getComments(postId)) {
                is NetworkResult.Success -> _detailState.update {
                    it.copy(comments = result.data, commentsLoading = false)
                }
                is NetworkResult.Error -> _detailState.update { state ->
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
        _detailState.update { it.copy(commentInput = text) }
    }

    fun submitComment() {
        val detail = _detailState.value
        val postId = detail.selectedPost?.id ?: return
        val content = detail.commentInput.trim()
        if (content.isEmpty()) return

        viewModelScope.launch {
            _detailState.update { it.copy(commentInput = "") }
            when (val result = repository.addComment(postId, content)) {
                is NetworkResult.Success -> {
                    _detailState.update { it.copy(comments = it.comments + result.data) }
                    _posts.update { posts ->
                        posts.map { p ->
                            if (p.id == postId) p.copy(commentCount = p.commentCount + 1) else p
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
