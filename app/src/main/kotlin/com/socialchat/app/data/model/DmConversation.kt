package com.socialchat.app.data.model

data class DmConversation(
    val id: Int,
    val partnerId: Int,
    val partnerUsername: String,
    val partnerAvatar: String?,
    val partnerPublicKey: String?,
    val updatedAt: String?,
    val lastMessageId: Int? = null,
    // Populated client-side after decryption
    val lastMessagePreview: String = ""
)
