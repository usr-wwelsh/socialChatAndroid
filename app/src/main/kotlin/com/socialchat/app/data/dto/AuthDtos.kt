package com.socialchat.app.data.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val message: String? = null,
    val user: UserDto
)

data class RegisterResponse(
    val message: String? = null,
    val user: UserDto
)

data class SessionResponse(
    @SerializedName("isAuthenticated") val authenticated: Boolean = false,
    val user: UserDto? = null
)

data class UserDto(
    val id: Int = 0,
    val username: String = "",
    val email: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null,
    val bio: String? = null
)
