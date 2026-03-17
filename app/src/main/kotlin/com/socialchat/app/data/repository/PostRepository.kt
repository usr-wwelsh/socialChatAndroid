package com.socialchat.app.data.repository

import android.util.Log
import com.socialchat.app.core.network.NetworkResult
import com.socialchat.app.core.network.safeApiCall
import com.socialchat.app.data.api.PostApiService
import com.socialchat.app.data.dto.CreateCommentRequest
import com.socialchat.app.data.dto.CreatePostRequest
import com.socialchat.app.data.dto.FeedResponse
import com.socialchat.app.data.dto.MediaResponse
import com.socialchat.app.data.model.Comment
import com.socialchat.app.data.model.Post
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val api: PostApiService
) {
    private var cachedFeed: List<Post>? = null
    private var cachedPage: Int = 0
    private var cacheTimestamp: Long = 0L
    private val cacheTtlMs = 60_000L

    suspend fun getFeed(page: Int = 1, limit: Int = 20, refresh: Boolean = false): NetworkResult<FeedResponse> {
        if (!refresh && cachedFeed != null && cachedPage == page) {
            val age = System.currentTimeMillis() - cacheTimestamp
            if (age < cacheTtlMs) {
                return NetworkResult.Success(FeedResponse(posts = cachedFeed, hasMore = true))
            }
        }
        if (refresh) {
            cachedFeed = null
            cachedPage = 0
        }
        val result = safeApiCall {
            val offset = (page - 1) * limit
            val resp = api.getFeed(offset, limit)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }
        if (result is NetworkResult.Success) {
            cachedFeed = result.data.posts ?: emptyList()
            cachedPage = page
            cacheTimestamp = System.currentTimeMillis()
        }
        return result
    }

    fun updateCachedPost(id: Int, transform: (Post) -> Post) {
        cachedFeed = cachedFeed?.map { if (it.id == id) transform(it) else it }
    }

    suspend fun getPost(id: Int): NetworkResult<Post> =
        safeApiCall {
            val resp = api.getPost(id)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }

    suspend fun getPostMedia(id: Int): NetworkResult<MediaResponse> =
        safeApiCall {
            val resp = api.getPostMedia(id)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }

    suspend fun createPost(request: CreatePostRequest): NetworkResult<Post> =
        safeApiCall {
            val resp = api.createPost(request)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
        }

    suspend fun deletePost(id: Int): NetworkResult<Unit> =
        safeApiCall {
            api.deletePost(id)
        }

    suspend fun toggleLike(id: Int, isLiked: Boolean): NetworkResult<Unit> {
        val result = safeApiCall {
            if (isLiked) api.unlikePost(id) else api.likePost(id)
            Unit
        }
        if (result is NetworkResult.Success) {
            cachedFeed = cachedFeed?.map { p ->
                if (p.id == id) p.copy(
                    isLiked = !isLiked,
                    likeCount = if (isLiked) p.likeCount - 1 else p.likeCount + 1
                ) else p
            }
        }
        return result
    }

    suspend fun getComments(postId: Int): NetworkResult<List<Comment>> =
        safeApiCall {
            val resp = api.getComments(postId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            val body = resp.body()!!
            Log.d("PostRepository", "getComments response: comments=${body.comments.size}")
            body.comments
        }

    suspend fun addComment(postId: Int, content: String): NetworkResult<Comment> {
        val result = safeApiCall {
            val resp = api.addComment(CreateCommentRequest(postId, content))
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.comment
        }
        if (result is NetworkResult.Success) {
            cachedFeed = cachedFeed?.map { p ->
                if (p.id == postId) p.copy(commentCount = p.commentCount + 1) else p
            }
        }
        return result
    }
}
