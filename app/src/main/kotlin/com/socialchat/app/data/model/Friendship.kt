package com.socialchat.app.data.model

import com.google.gson.annotations.SerializedName

data class Friendship(
    val id: Int = 0,
    @SerializedName("requester_id") val requesterId: Int = 0,
    @SerializedName("receiver_id") val receiverId: Int = 0,
    val requester: com.socialchat.app.data.model.User? = null,
    val status: String = "",
    @SerializedName("created_at") val createdAt: String? = null
)
