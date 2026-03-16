package com.socialchat.app.data.repository

import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.safeApiCall
import com.socialchat.app.data.api.FriendApiService
import com.socialchat.app.data.dto.FriendStatusResponse
import com.socialchat.app.data.dto.SendFriendRequestBody
import com.socialchat.app.data.model.Friendship
import com.socialchat.app.data.model.User
import javax.inject.Inject

class FriendRepository @Inject constructor(
    private val api: FriendApiService
) {
    suspend fun getUserFriends(userId: Int): NetworkResult<List<User>> =
        safeApiCall {
            val resp = api.getUserFriends(userId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.friends?.map {
                User(id = it.friendId, username = it.username, profilePicture = it.profilePicture, bio = it.bio)
            } ?: emptyList()
        }

    suspend fun getFriendRequests(): NetworkResult<List<Friendship>> =
        safeApiCall {
            val resp = api.getFriendRequests()
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.requests ?: emptyList()
        }

    suspend fun sendFriendRequest(userId: Int): NetworkResult<Unit> =
        safeApiCall {
            val resp = api.sendFriendRequest(SendFriendRequestBody(userId))
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
        }

    suspend fun acceptFriendRequest(requestId: Int): NetworkResult<Unit> =
        safeApiCall {
            val resp = api.acceptFriendRequest(requestId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
        }

    suspend fun rejectFriendRequest(requestId: Int): NetworkResult<Unit> =
        safeApiCall {
            val resp = api.rejectFriendRequest(requestId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
        }

    suspend fun removeFriend(friendshipId: Int): NetworkResult<Unit> =
        safeApiCall {
            val resp = api.removeFriend(friendshipId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
        }

    suspend fun getFriendStatus(userId: Int): NetworkResult<FriendStatusResponse> =
        safeApiCall {
            val resp = api.getFriendStatus(userId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }
}
