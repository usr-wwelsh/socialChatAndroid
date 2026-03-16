package com.socialchat.app.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    val id: Int = 0,
    @SerializedName("chatroom_id") val roomId: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    val username: String = "",
    @SerializedName("profile_picture") val profilePicture: String? = null,
    @SerializedName("message") val content: String = "",
    @SerializedName("created_at") val createdAt: String? = null,
    val pending: Boolean = false
)
