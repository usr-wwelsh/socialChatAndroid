package com.socialchat.app.data.api

import com.socialchat.app.data.dto.ChatroomsResponse
import com.socialchat.app.data.dto.MessagesResponse
import retrofit2.Response
import retrofit2.http.*

interface ChatroomApiService {
    @GET("api/chatrooms")
    suspend fun getRooms(): Response<ChatroomsResponse>

    @GET("api/chatrooms/{id}/messages")
    suspend fun getMessages(
        @Path("id") roomId: Int,
        @Query("limit") limit: Int = 100
    ): Response<MessagesResponse>
}
