package com.socialchat.app.data.repository

import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.safeApiCall
import com.socialchat.app.data.api.ChatroomApiService
import com.socialchat.app.data.model.ChatMessage
import com.socialchat.app.data.model.Chatroom
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val api: ChatroomApiService
) {
    suspend fun getRooms(): NetworkResult<List<Chatroom>> =
        safeApiCall {
            val resp = api.getRooms()
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.chatrooms ?: emptyList()
        }

    suspend fun getMessages(roomId: Int): NetworkResult<List<ChatMessage>> =
        safeApiCall {
            val resp = api.getMessages(roomId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.messages ?: emptyList()
        }
}
