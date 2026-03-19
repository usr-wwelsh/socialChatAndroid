package com.socialchat.app.data.dto

import com.google.gson.annotations.SerializedName

// --- Conversations ---

data class ConversationsResponse(
    val conversations: List<DmConversationDto>? = null
)

data class DmConversationDto(
    val id: Int,
    @SerializedName("partner_id") val partnerId: Int,
    @SerializedName("partner_username") val partnerUsername: String,
    @SerializedName("partner_avatar") val partnerAvatar: String?,
    @SerializedName("partner_public_key") val partnerPublicKey: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("last_message_id") val lastMessageId: Int? = null
)

data class CreateConversationRequest(
    val partnerId: Int
)

data class CreateConversationResponse(
    val conversation: DmConversationDto? = null
)

// --- Messages ---

data class DmMessagesResponse(
    val messages: List<DmMessageDto>? = null
)

data class DmMessageDto(
    val id: Int,
    @SerializedName("conversation_id") val conversationId: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("sender_username") val senderUsername: String,
    @SerializedName("sender_avatar") val senderAvatar: String?,
    val ciphertext: String,
    val iv: String,
    @SerializedName("created_at") val createdAt: String?
)

data class SendDmRequest(
    val ciphertext: String,
    val iv: String
)

data class SendDmResponse(
    val message: DmMessageDto? = null,
    val updatedAt: String? = null,
    val lastMessageId: Int? = null
)

// --- Keys ---

data class MyKeyResponse(
    val encryptedPrivateKey: String?,
    val keyIv: String?,
    val keySalt: String?
)

data class PublicKeyResponse(
    val publicKey: String?
)

data class StoreKeysRequest(
    val publicKey: String,
    val encryptedPrivateKey: String,
    val keyIv: String,
    val keySalt: String
)
