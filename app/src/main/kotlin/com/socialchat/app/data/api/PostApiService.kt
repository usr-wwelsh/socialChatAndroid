package com.socialchat.app.data.api

import com.socialchat.app.data.dto.CommentsResponse
import com.socialchat.app.data.dto.CreateCommentRequest
import com.socialchat.app.data.dto.ReactRequest
import com.socialchat.app.data.dto.CreateCommentResponse
import com.socialchat.app.data.dto.CreatePostRequest
import com.socialchat.app.data.dto.EditPostRequest
import com.socialchat.app.data.dto.FeedResponse
import com.socialchat.app.data.dto.MediaResponse
import com.socialchat.app.data.model.Post
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PostApiService {
    @GET("api/posts")
    suspend fun getFeed(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<FeedResponse>

    @GET("api/posts/{id}")
    suspend fun getPost(@Path("id") id: Int): Response<Post>

    @GET("api/posts/{id}/media")
    suspend fun getPostMedia(@Path("id") id: Int): Response<MediaResponse>

    @POST("api/posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<Post>

    @Multipart
    @POST("api/posts")
    suspend fun createPostWithMedia(
        @Part("content") content: RequestBody,
        @Part("media_type") mediaType: RequestBody,
        @Part("visibility") visibility: RequestBody,
        @Part("tags") tags: RequestBody,
        @Part media: MultipartBody.Part
    ): Response<Post>

    @PUT("api/posts/{id}")
    suspend fun editPost(@Path("id") id: Int, @Body request: EditPostRequest): Response<Post>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") id: Int): Response<Unit>

    @POST("api/posts/{id}/react")
    suspend fun likePost(@Path("id") id: Int, @Body body: ReactRequest = ReactRequest("like")): Response<Unit>

    @DELETE("api/posts/{id}/react/like")
    suspend fun unlikePost(@Path("id") id: Int): Response<Unit>

    @GET("api/comments/post/{id}")
    suspend fun getComments(@Path("id") id: Int): Response<CommentsResponse>

    @POST("api/comments")
    suspend fun addComment(@Body body: CreateCommentRequest): Response<CreateCommentResponse>
}
