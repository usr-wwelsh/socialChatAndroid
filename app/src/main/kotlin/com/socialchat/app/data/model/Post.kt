package com.socialchat.app.data.model

import com.google.gson.annotations.SerializedName

data class Post(
    val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    val username: String? = null,
    @SerializedName("user_profile_picture") val profilePicture: String? = null,
    val content: String? = null,
    @SerializedName("media_type") val mediaType: String? = null,
    @SerializedName("media_url") val mediaUrl: String? = null,
    val visibility: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("reaction_count") val likeCount: Int = 0,
    @SerializedName("comment_count") val commentCount: Int = 0,
    @SerializedName("is_liked") val isLiked: Boolean = false,
    val tags: List<Tag>? = null,
    @SerializedName("preview_comments") val previewComments: List<Comment>? = null
)
