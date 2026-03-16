package com.socialchat.app.data.dto

import com.google.gson.annotations.SerializedName
import com.socialchat.app.data.model.Comment
import com.socialchat.app.data.model.Post

data class CreatePostRequest(
    val content: String,
    @SerializedName("media_type") val mediaType: String? = null,
    @SerializedName("media_data") val mediaData: String? = null,
    val visibility: String = "public",
    val tags: List<String> = emptyList()
)

data class EditPostRequest(
    val content: String,
    val visibility: String = "public",
    val tags: List<String> = emptyList()
)

data class FeedResponse(
    val posts: List<Post>? = null,
    val page: Int = 0,
    @SerializedName("total_pages") val totalPages: Int = 0,
    @SerializedName("has_more") val hasMore: Boolean = false
)

data class MediaResponse(
    @SerializedName("media_type") val mediaType: String,
    @SerializedName("media_data") val mediaData: String
)

data class CommentsResponse(
    val comments: List<Comment> = emptyList()
)

data class CreateCommentRequest(
    @SerializedName("post_id") val postId: Int,
    val content: String
)

data class CreateCommentResponse(
    val message: String? = null,
    val comment: Comment
)
