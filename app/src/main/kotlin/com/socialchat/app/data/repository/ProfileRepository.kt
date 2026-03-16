package com.socialchat.app.data.repository

import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.safeApiCall
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.data.api.ProfileApiService
import com.socialchat.app.data.dto.ProfileResponse
import com.socialchat.app.data.dto.UpdateProfileRequest
import com.socialchat.app.data.dto.UpdateProfileResponse
import com.socialchat.app.data.model.User
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val api: ProfileApiService,
    private val prefs: UserPreferences
) {
    suspend fun getMyProfile(): NetworkResult<ProfileResponse> =
        safeApiCall {
            val username = prefs.getCachedUsername()
                ?: throw IllegalStateException("No cached username")
            val resp = api.getUserProfile(username)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }

    suspend fun getUserProfile(username: String): NetworkResult<ProfileResponse> =
        safeApiCall {
            val resp = api.getUserProfile(username)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }

    suspend fun updateProfile(bio: String?, profilePicture: String?): NetworkResult<User> =
        safeApiCall {
            val resp = api.updateProfile(UpdateProfileRequest(bio, profilePicture))
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.user
        }
}
