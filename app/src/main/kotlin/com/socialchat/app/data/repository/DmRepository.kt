package com.socialchat.app.data.repository

import android.util.Log
import com.socialchat.app.core.crypto.CryptoManager
import com.socialchat.app.core.crypto.E2ECrypto
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.safeApiCall
import com.socialchat.app.data.api.DmApiService
import com.socialchat.app.data.dto.CreateConversationRequest
import com.socialchat.app.data.dto.SendDmRequest
import com.socialchat.app.data.model.DmConversation
import com.socialchat.app.data.model.DmMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DmRepository @Inject constructor(
    private val api: DmApiService,
    private val cryptoManager: CryptoManager
) {
    private val tag = "DmRepository"

    // Simple cache
    private var cachedConversations: List<DmConversation>? = null
    private var conversationsTimestamp: Long = 0L
    private val messagesCache = mutableMapOf<Int, Pair<List<DmMessage>, Long>>()
    private val cacheTtlMs = 30_000L

    suspend fun getConversations(forceRefresh: Boolean = false): NetworkResult<List<DmConversation>> {
        val cached = cachedConversations
        if (!forceRefresh && cached != null && System.currentTimeMillis() - conversationsTimestamp < cacheTtlMs) {
            return NetworkResult.Success(cached)
        }
        return safeApiCall {
            val resp = api.getConversations()
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            val dtos = resp.body()?.conversations ?: emptyList()
            val conversations = dtos.map { dto ->
                DmConversation(
                    id = dto.id,
                    partnerId = dto.partnerId,
                    partnerUsername = dto.partnerUsername,
                    partnerAvatar = dto.partnerAvatar,
                    partnerPublicKey = dto.partnerPublicKey,
                    updatedAt = dto.updatedAt,
                    lastMessageId = dto.lastMessageId
                )
            }
            cachedConversations = conversations
            conversationsTimestamp = System.currentTimeMillis()
            conversations
        }
    }

    suspend fun getOrCreateConversation(partnerId: Int): NetworkResult<DmConversation> =
        safeApiCall {
            val resp = api.createConversation(CreateConversationRequest(partnerId))
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            val dto = resp.body()?.conversation ?: throw Exception("Empty response")
            DmConversation(
                id = dto.id,
                partnerId = dto.partnerId ?: partnerId,
                partnerUsername = dto.partnerUsername ?: "",
                partnerAvatar = dto.partnerAvatar,
                partnerPublicKey = dto.partnerPublicKey,
                updatedAt = dto.updatedAt,
                lastMessageId = dto.lastMessageId
            )
        }

    suspend fun getMessages(
        conversationId: Int,
        partnerPublicKey: String?,
        forceRefresh: Boolean = false
    ): NetworkResult<List<DmMessage>> {
        val entry = messagesCache[conversationId]
        if (!forceRefresh && entry != null && System.currentTimeMillis() - entry.second < cacheTtlMs) {
            return NetworkResult.Success(entry.first)
        }
        return safeApiCall {
            val resp = api.getMessages(conversationId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            val dtos = resp.body()?.messages ?: emptyList()
            val sharedKey = cryptoManager.getSharedKey(conversationId, partnerPublicKey)
            val messages = dtos.map { dto ->
                val content = if (sharedKey != null) {
                    try {
                        E2ECrypto.decryptMessage(dto.ciphertext, dto.iv, sharedKey)
                    } catch (e: Exception) {
                        Log.w(tag, "Failed to decrypt message ${dto.id}", e)
                        "[encrypted]"
                    }
                } else {
                    "[encrypted]"
                }
                DmMessage(
                    id = dto.id,
                    conversationId = dto.conversationId,
                    senderId = dto.senderId,
                    senderUsername = dto.senderUsername,
                    senderAvatar = dto.senderAvatar,
                    ciphertext = dto.ciphertext,
                    iv = dto.iv,
                    createdAt = dto.createdAt,
                    content = content
                )
            }
            messagesCache[conversationId] = messages to System.currentTimeMillis()
            messages
        }
    }

    suspend fun sendMessage(
        conversationId: Int,
        partnerPublicKey: String?,
        plaintext: String
    ): NetworkResult<DmMessage> {
        val sharedKey = cryptoManager.getSharedKey(conversationId, partnerPublicKey)
            ?: return NetworkResult.Error("Encryption not available — please log out and back in")

        return safeApiCall {
            val encrypted = E2ECrypto.encryptMessage(plaintext, sharedKey)
            val resp = api.sendMessage(conversationId, SendDmRequest(encrypted.ciphertext, encrypted.iv))
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            val dto = resp.body()?.message ?: throw Exception("Empty response")
            val message = DmMessage(
                id = dto.id,
                conversationId = dto.conversationId,
                senderId = dto.senderId,
                senderUsername = dto.senderUsername,
                senderAvatar = dto.senderAvatar,
                ciphertext = dto.ciphertext,
                iv = dto.iv,
                createdAt = dto.createdAt,
                content = plaintext
            )
            appendCachedMessage(conversationId, message)
            message
        }
    }

    fun appendCachedMessage(conversationId: Int, message: DmMessage) {
        val entry = messagesCache[conversationId] ?: return
        messagesCache[conversationId] = (entry.first + message) to entry.second
    }

    fun updateConversationTimestamp(conversationId: Int, updatedAt: String) {
        cachedConversations = cachedConversations?.map { conv ->
            if (conv.id == conversationId) conv.copy(updatedAt = updatedAt) else conv
        }
    }

    fun invalidateConversations() {
        conversationsTimestamp = 0L
    }
}
