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
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: ProfileApiService,
    private val prefs: UserPreferences
) {
    private var cachedMyProfile: ProfileResponse? = null
    private var myProfileTimestamp: Long = 0L
    private val userProfileCache = mutableMapOf<String, Pair<ProfileResponse, Long>>()
    private val cacheTtlMs = 60_000L

    suspend fun getMyProfile(): NetworkResult<ProfileResponse> {
        val cached = cachedMyProfile
        if (cached != null && System.currentTimeMillis() - myProfileTimestamp < cacheTtlMs) {
            return NetworkResult.Success(cached)
        }
        val result = safeApiCall {
            val username = prefs.getCachedUsername()
                ?: throw IllegalStateException("No cached username")
            val resp = api.getUserProfile(username)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }
        if (result is NetworkResult.Success) {
            cachedMyProfile = result.data
            myProfileTimestamp = System.currentTimeMillis()
        }
        return result
    }

    suspend fun getUserProfile(username: String): NetworkResult<ProfileResponse> {
        val entry = userProfileCache[username]
        if (entry != null && System.currentTimeMillis() - entry.second < cacheTtlMs) {
            return NetworkResult.Success(entry.first)
        }
        val result = safeApiCall {
            val resp = api.getUserProfile(username)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }
        if (result is NetworkResult.Success) {
            userProfileCache[username] = result.data to System.currentTimeMillis()
        }
        return result
    }

    suspend fun updateProfile(bio: String?, profilePicture: String?): NetworkResult<User> {
        val result = safeApiCall {
            val resp = api.updateProfile(UpdateProfileRequest(bio, profilePicture))
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.user
        }
        if (result is NetworkResult.Success) {
            cachedMyProfile = null
        }
        return result
    }
}
