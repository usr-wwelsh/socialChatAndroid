package com.socialchat.app.data.api

import com.socialchat.app.data.dto.LoginRequest
import com.socialchat.app.data.dto.LoginResponse
import com.socialchat.app.data.dto.RegisterRequest
import com.socialchat.app.data.dto.RegisterResponse
import com.socialchat.app.data.dto.SessionResponse
import com.socialchat.app.data.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("api/auth/session")
    suspend fun checkSession(): Response<SessionResponse>
}
