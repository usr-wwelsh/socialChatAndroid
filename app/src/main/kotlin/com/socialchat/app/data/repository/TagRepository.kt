package com.socialchat.app.data.repository

import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.safeApiCall
import com.socialchat.app.data.api.TagApiService
import com.socialchat.app.data.model.Post
import com.socialchat.app.data.model.Tag
import com.socialchat.app.data.model.User
import javax.inject.Inject

class TagRepository @Inject constructor(
    private val api: TagApiService
) {
    suspend fun getTrendingTags(): NetworkResult<List<Tag>> =
        safeApiCall {
            val resp = api.getTrendingTags()
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }

    suspend fun getPostsByTag(tagName: String, page: Int = 1): NetworkResult<List<Post>> =
        safeApiCall {
            val resp = api.getPostsByTag(tagName, page)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }

    suspend fun searchUsers(query: String): NetworkResult<List<User>> =
        safeApiCall {
            val resp = api.searchUsers(query)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.users ?: emptyList()
        }
}
