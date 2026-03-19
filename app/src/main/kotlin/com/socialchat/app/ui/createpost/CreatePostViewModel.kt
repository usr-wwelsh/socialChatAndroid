package com.socialchat.app.ui.createpost

import android.content.Context
import android.net.Uri
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class CreatePostUiState(
    val content: String = "",
    val mediaUri: Uri? = null,
    val mediaType: String? = null,
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
                val size = context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
                if (size > 10 * 1024 * 1024) {
                    _uiState.update { it.copy(error = "File too large (max 10MB)") }
                    return@launch
                }
                val mediaCategory = mimeType.substringBefore("/")
                _uiState.update { it.copy(mediaUri = uri, mediaType = mediaCategory) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load media: ${e.message}") }
            }
        }
    }

    fun clearMedia() = _uiState.update { it.copy(mediaUri = null, mediaType = null) }

    fun submitPost(context: Context) {
        val state = _uiState.value
        if (state.content.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val mediaPart = if (state.mediaUri != null && state.mediaType != null) {
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(state.mediaUri)?.readBytes()
                    }
                    if (bytes == null) {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to read media file") }
                        return@launch
                    }
                    val mimeType = context.contentResolver.getType(state.mediaUri) ?: "application/octet-stream"
                    val ext = mimeType.substringAfter("/").substringBefore(";")
                    MultipartBody.Part.createFormData("media", "upload.$ext", bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to attach media") }
                    return@launch
                }
            } else null

            val request = CreatePostRequest(
                content = state.content,
                mediaType = state.mediaType,
                visibility = state.visibility,
                tags = state.tags
            )
            when (val result = postRepository.createPost(request, mediaPart)) {
                is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, posted = true) }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }
}
