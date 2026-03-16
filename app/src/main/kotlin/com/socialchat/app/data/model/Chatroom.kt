package com.socialchat.app.data.model

import com.google.gson.annotations.SerializedName

data class Chatroom(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("created_by") val createdBy: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("last_message") val lastMessage: String? = null,
    @SerializedName("last_message_time") val lastMessageTime: String? = null,
    @SerializedName("unread_count") val unreadCount: Int = 0
)
