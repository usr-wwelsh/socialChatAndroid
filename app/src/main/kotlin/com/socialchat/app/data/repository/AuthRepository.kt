package com.socialchat.app.data.repository

import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.RetrofitProvider
import com.socialchat.app.core.network.safeApiCall
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.data.api.AuthApiService
import com.socialchat.app.data.dto.LoginRequest
import com.socialchat.app.data.dto.RegisterRequest
import com.socialchat.app.data.dto.SessionResponse
import com.socialchat.app.data.dto.UserDto
import com.socialchat.app.data.dto.LoginResponse
import com.socialchat.app.data.dto.RegisterResponse
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val retrofitProvider: RetrofitProvider,
    private val prefs: UserPreferences
) {
    private suspend fun api(): AuthApiService {
        val url = prefs.getBaseUrl()
        return retrofitProvider.getRetrofit(url).create(AuthApiService::class.java)
    }

    suspend fun login(username: String, password: String): NetworkResult<UserDto> =
        safeApiCall {
            val resp = api().login(LoginRequest(username, password))
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.user
        }

    suspend fun register(username: String, password: String): NetworkResult<UserDto> =
        safeApiCall {
            val resp = api().register(RegisterRequest(username, password))
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.user
        }

    suspend fun logout(): NetworkResult<Unit> =
        safeApiCall {
            api().logout()
        }

    suspend fun checkSession(): NetworkResult<SessionResponse> =
        safeApiCall {
            val resp = api().checkSession()
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }
}
