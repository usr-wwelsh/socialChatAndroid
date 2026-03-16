package com.socialchat.app.data.dto

import com.google.gson.annotations.SerializedName
import com.socialchat.app.data.model.Friendship

data class UserFriendsResponse(val friends: List<FriendItem>? = null)

data class FriendItem(
    val id: Int = 0,
    @SerializedName("friend_id") val friendId: Int = 0,
    val username: String = "",
    @SerializedName("profile_picture") val profilePicture: String? = null,
    val bio: String? = null
)

data class FriendRequestsResponse(val requests: List<Friendship>? = null)

data class SendFriendRequestBody(@SerializedName("receiver_id") val receiverId: Int)

data class FriendStatusResponse(
    val status: String = "none",
    @SerializedName("friendshipId") val friendshipId: Int? = null,
    @SerializedName("isRequester") val isRequester: Boolean? = null
)
