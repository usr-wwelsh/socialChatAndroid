package com.socialchat.app.data.repository

import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.safeApiCall
import com.socialchat.app.data.api.ChatroomApiService
import com.socialchat.app.data.model.ChatMessage
import com.socialchat.app.data.model.Chatroom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: ChatroomApiService
) {
    private var cachedRooms: List<Chatroom>? = null
    private var roomsTimestamp: Long = 0L
    private val messagesCache = mutableMapOf<Int, Pair<List<ChatMessage>, Long>>()
    private val roomsCacheTtlMs = 60_000L
    private val messagesCacheTtlMs = 30_000L

    suspend fun getRooms(): NetworkResult<List<Chatroom>> {
        val cached = cachedRooms
        if (cached != null && System.currentTimeMillis() - roomsTimestamp < roomsCacheTtlMs) {
            return NetworkResult.Success(cached)
        }
        val result = safeApiCall {
            val resp = api.getRooms()
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.chatrooms ?: emptyList()
        }
        if (result is NetworkResult.Success) {
            cachedRooms = result.data
            roomsTimestamp = System.currentTimeMillis()
        }
        return result
    }

    suspend fun getMessages(roomId: Int): NetworkResult<List<ChatMessage>> {
        val entry = messagesCache[roomId]
        if (entry != null && System.currentTimeMillis() - entry.second < messagesCacheTtlMs) {
            return NetworkResult.Success(entry.first)
        }
        val result = safeApiCall {
            val resp = api.getMessages(roomId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.messages ?: emptyList()
        }
        if (result is NetworkResult.Success) {
            messagesCache[roomId] = result.data to System.currentTimeMillis()
        }
        return result
    }

    fun appendCachedMessage(roomId: Int, message: ChatMessage) {
        val entry = messagesCache[roomId] ?: return
        messagesCache[roomId] = (entry.first + message) to entry.second
    }

    fun updateRoomLastMessage(roomId: Int, message: ChatMessage) {
        cachedRooms = cachedRooms?.map { room ->
            if (room.id == roomId) room.copy(
                lastMessage = message.content,
                lastMessageTime = message.createdAt
            ) else room
        }
    }
}
