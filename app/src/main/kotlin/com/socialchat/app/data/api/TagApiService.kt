package com.socialchat.app.data.api

import com.socialchat.app.data.dto.SearchUsersResponse
import com.socialchat.app.data.model.Post
import com.socialchat.app.data.model.Tag
import retrofit2.Response
import retrofit2.http.*

interface TagApiService {
    @GET("api/tags/trending")
    suspend fun getTrendingTags(): Response<List<Tag>>

    @GET("api/tags/{name}/posts")
    suspend fun getPostsByTag(
        @Path("name") tagName: String,
        @Query("page") page: Int = 1
    ): Response<List<Post>>

    @GET("api/profiles/search")
    suspend fun searchUsers(
        @Query("q") query: String
    ): Response<SearchUsersResponse>
}
