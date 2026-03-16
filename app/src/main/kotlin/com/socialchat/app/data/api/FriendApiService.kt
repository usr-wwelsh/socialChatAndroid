package com.socialchat.app.data.api

import com.socialchat.app.data.dto.FriendRequestsResponse
import com.socialchat.app.data.dto.FriendStatusResponse
import com.socialchat.app.data.dto.SendFriendRequestBody
import com.socialchat.app.data.dto.UserFriendsResponse
import retrofit2.Response
import retrofit2.http.*

interface FriendApiService {
    @GET("api/friends/user/{userId}")
    suspend fun getUserFriends(@Path("userId") userId: Int): Response<UserFriendsResponse>

    @GET("api/friends/requests/received")
    suspend fun getFriendRequests(): Response<FriendRequestsResponse>

    @POST("api/friends/request")
    suspend fun sendFriendRequest(@Body body: SendFriendRequestBody): Response<Unit>

    @PUT("api/friends/{id}/accept")
    suspend fun acceptFriendRequest(@Path("id") requestId: Int): Response<Unit>

    @PUT("api/friends/{id}/reject")
    suspend fun rejectFriendRequest(@Path("id") requestId: Int): Response<Unit>

    @DELETE("api/friends/{friendshipId}")
    suspend fun removeFriend(@Path("friendshipId") friendshipId: Int): Response<Unit>

    @GET("api/friends/status/{userId}")
    suspend fun getFriendStatus(@Path("userId") userId: Int): Response<FriendStatusResponse>
}
