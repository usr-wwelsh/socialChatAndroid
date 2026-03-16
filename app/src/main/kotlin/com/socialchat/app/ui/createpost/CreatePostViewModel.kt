package com.socialchat.app.ui.createpost

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.data.dto.CreatePostRequest
import com.socialchat.app.data.repository.PostRepository
import com.socialchat.app.data.repository.TagRepository
import com.socialchat.app.data.model.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CreatePostUiState(
    val content: String = "",
    val mediaUri: Uri? = null,
    val mediaType: String? = null,
    val mediaData: String? = null,
    val visibility: String = "public",
    val tagInput: String = "",
    val tags: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val posted: Boolean = false
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun updateContent(content: String) = _uiState.update { it.copy(content = content) }
    fun updateVisibility(visibility: String) = _uiState.update { it.copy(visibility = visibility) }
    fun updateTagInput(input: String) = _uiState.update { it.copy(tagInput = input) }

    fun addTag(tag: String) {
        val clean = tag.trim().removePrefix("#").lowercase()
        if (clean.isNotEmpty() && !_uiState.value.tags.contains(clean)) {
            _uiState.update { it.copy(tags = it.tags + clean, tagInput = "") }
        }
    }

    fun removeTag(tag: String) = _uiState.update { it.copy(tags = it.tags - tag) }

    fun setMedia(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                if (bytes.size > 10 * 1024 * 1024) {
                    _uiState.update { it.copy(error = "File too large (max 10MB)") }
                    return@launch
                }
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val dataUri = "data:$mimeType;base64,$base64"
                // Backend expects "image", "video", or "audio" — not the full MIME type
                val mediaCategory = mimeType.substringBefore("/")
                _uiState.update { it.copy(mediaUri = uri, mediaType = mediaCategory, mediaData = dataUri) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load media: ${e.message}") }
            }
        }
    }

    fun clearMedia() = _uiState.update { it.copy(mediaUri = null, mediaType = null, mediaData = null) }

    fun submitPost() {
        val state = _uiState.value
        if (state.content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val request = CreatePostRequest(
                content = state.content,
                mediaType = state.mediaType,
                mediaData = state.mediaData,
                visibility = state.visibility,
                tags = state.tags
            )
            when (val result = postRepository.createPost(request)) {
                is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, posted = true) }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }
}
