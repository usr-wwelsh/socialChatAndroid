package com.socialchat.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.data.model.Post
import com.socialchat.app.data.model.Tag
import com.socialchat.app.data.model.User
import com.socialchat.app.data.repository.PostRepository
import com.socialchat.app.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val query: String = "",
    val trendingTags: List<Tag> = emptyList(),
    val postResults: List<Post> = emptyList(),
    val userResults: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val mediaPosts: List<Post> = emptyList(),
    val isLoadingMedia: Boolean = false,
    val hasMoreMedia: Boolean = true,
    val mediaPage: Int = 1
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val tagRepository: TagRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadTrendingTags()
        loadMediaPosts()
    }

    private fun loadTrendingTags() {
        viewModelScope.launch {
            when (val result = tagRepository.getTrendingTags()) {
                is NetworkResult.Success -> _uiState.update { it.copy(trendingTags = result.data) }
                else -> {}
            }
        }
    }

    private fun loadMediaPosts() {
        val state = _uiState.value
        if (state.isLoadingMedia || !state.hasMoreMedia) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMedia = true) }
            when (val result = postRepository.getMediaFeed(state.mediaPage)) {
                is NetworkResult.Success -> {
                    val newPosts = result.data.posts ?: emptyList()
                    _uiState.update {
                        it.copy(
                            mediaPosts = it.mediaPosts + newPosts,
                            isLoadingMedia = false,
                            hasMoreMedia = result.data.hasMore,
                            mediaPage = it.mediaPage + 1
                        )
                    }
                }
                else -> _uiState.update { it.copy(isLoadingMedia = false) }
            }
        }
    }

    fun loadMoreMediaIfNeeded(lastVisibleIndex: Int) {
        val state = _uiState.value
        if (lastVisibleIndex >= state.mediaPosts.size - 9 && state.hasMoreMedia && !state.isLoadingMedia) {
            loadMediaPosts()
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(postResults = emptyList(), userResults = emptyList(), error = null) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(isLoading = true) }
            if (query.startsWith("#")) {
                val tag = query.removePrefix("#").trim()
                when (val result = tagRepository.getPostsByTag(tag)) {
                    is NetworkResult.Success -> _uiState.update { it.copy(postResults = result.data, userResults = emptyList(), isLoading = false) }
                    is NetworkResult.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                    else -> {}
                }
            } else {
                when (val result = tagRepository.searchUsers(query)) {
                    is NetworkResult.Success -> _uiState.update { it.copy(userResults = result.data, postResults = emptyList(), isLoading = false) }
                    is NetworkResult.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                    else -> {}
                }
            }
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            val updated = post.copy(
                isLiked = !post.isLiked,
                likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
            )
            _uiState.update {
                it.copy(
                    postResults = it.postResults.map { p -> if (p.id == post.id) updated else p },
                    mediaPosts = it.mediaPosts.map { p -> if (p.id == post.id) updated else p }
                )
            }
            val result = postRepository.toggleLike(post.id, post.isLiked)
            if (result is NetworkResult.Error) {
                _uiState.update {
                    it.copy(
                        postResults = it.postResults.map { p -> if (p.id == post.id) post else p },
                        mediaPosts = it.mediaPosts.map { p -> if (p.id == post.id) post else p }
                    )
                }
            }
        }
    }

    fun searchByTag(tagName: String) {
        _uiState.update { it.copy(query = "#$tagName", isLoading = true) }
        viewModelScope.launch {
            when (val result = tagRepository.getPostsByTag(tagName)) {
                is NetworkResult.Success -> _uiState.update { it.copy(postResults = result.data, userResults = emptyList(), isLoading = false) }
                is NetworkResult.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                else -> {}
            }
        }
    }
}
