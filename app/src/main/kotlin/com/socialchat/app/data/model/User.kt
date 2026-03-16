package com.socialchat.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    val email: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null,
    val bio: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("friend_count") val friendCount: Int = 0,
    @SerializedName("is_friend") val isFriend: Boolean = false,
    @SerializedName("friend_request_sent") val friendRequestSent: Boolean = false,
    val links: String? = null
)
