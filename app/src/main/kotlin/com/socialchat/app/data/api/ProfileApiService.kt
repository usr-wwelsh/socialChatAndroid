package com.socialchat.app.data.api

import com.socialchat.app.data.dto.ProfileResponse
import com.socialchat.app.data.dto.UpdateProfileRequest
import com.socialchat.app.data.dto.UpdateProfileResponse
import com.socialchat.app.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface ProfileApiService {
    @GET("api/profiles/{username}")
    suspend fun getUserProfile(@Path("username") username: String): Response<ProfileResponse>

    @PUT("api/profiles/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>
}
