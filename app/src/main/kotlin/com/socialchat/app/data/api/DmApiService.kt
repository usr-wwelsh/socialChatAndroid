package com.socialchat.app.data.api

import com.socialchat.app.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface DmApiService {
    @GET("api/dms/conversations")
    suspend fun getConversations(): Response<ConversationsResponse>

    @POST("api/dms/conversations")
    suspend fun createConversation(@Body request: CreateConversationRequest): Response<CreateConversationResponse>

    @GET("api/dms/conversations/{id}/messages")
    suspend fun getMessages(
        @Path("id") conversationId: Int,
        @Query("limit") limit: Int = 100
    ): Response<DmMessagesResponse>

    @POST("api/dms/conversations/{id}/messages")
    suspend fun sendMessage(
        @Path("id") conversationId: Int,
        @Body request: SendDmRequest
    ): Response<SendDmResponse>
}
