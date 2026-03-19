package com.socialchat.app.data.model

data class DmMessage(
    val id: Int = 0,
    val conversationId: Int = 0,
    val senderId: Int = 0,
    val senderUsername: String = "",
    val senderAvatar: String? = null,
    val ciphertext: String = "",
    val iv: String = "",
    val createdAt: String? = null,
    // Populated client-side after decryption
    val content: String = "",
    val pending: Boolean = false
)
