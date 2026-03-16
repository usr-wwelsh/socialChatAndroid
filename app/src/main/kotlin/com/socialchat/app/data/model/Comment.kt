package com.socialchat.app.data.model

import com.google.gson.annotations.SerializedName

data class Comment(
    val id: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("user_id") val userId: Int,
    val username: String,
    @SerializedName("profile_picture") val profilePicture: String? = null,
    val content: String,
    @SerializedName("created_at") val createdAt: String? = null
)
