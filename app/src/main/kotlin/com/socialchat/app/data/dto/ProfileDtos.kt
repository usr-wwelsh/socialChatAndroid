package com.socialchat.app.data.dto

import com.google.gson.annotations.SerializedName
import com.socialchat.app.data.model.Post
import com.socialchat.app.data.model.User

data class UpdateProfileRequest(
    val bio: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null
)

data class ProfileResponse(
    val user: User,
    val posts: List<Post>? = null,
    val friends: List<User>? = null
)

data class UpdateProfileResponse(
    val message: String? = null,
    val user: User
)

data class SearchUsersResponse(val users: List<User>? = null)
