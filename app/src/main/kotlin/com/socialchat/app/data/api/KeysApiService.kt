package com.socialchat.app.data.api

import com.socialchat.app.data.dto.MyKeyResponse
import com.socialchat.app.data.dto.PublicKeyResponse
import com.socialchat.app.data.dto.StoreKeysRequest
import retrofit2.Response
import retrofit2.http.*

interface KeysApiService {
    @GET("api/keys/me")
    suspend fun getMyKeys(): Response<MyKeyResponse>

    @POST("api/keys")
    suspend fun storeKeys(@Body request: StoreKeysRequest): Response<Unit>

    @GET("api/keys/user/{userId}")
    suspend fun getUserPublicKey(@Path("userId") userId: Int): Response<PublicKeyResponse>
}
