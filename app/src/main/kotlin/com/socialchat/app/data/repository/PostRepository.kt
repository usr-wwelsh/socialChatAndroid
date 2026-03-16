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

class PostRepository @Inject constructor(
    private val api: PostApiService
) {
    suspend fun getFeed(page: Int = 1, limit: Int = 20): NetworkResult<FeedResponse> =
        safeApiCall {
            val offset = (page - 1) * limit
            val resp = api.getFeed(offset, limit)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!
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

    suspend fun toggleLike(id: Int, isLiked: Boolean): NetworkResult<Unit> =
        safeApiCall {
            if (isLiked) api.unlikePost(id) else api.likePost(id)
        }

    suspend fun getComments(postId: Int): NetworkResult<List<Comment>> =
        safeApiCall {
            val resp = api.getComments(postId)
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            val body = resp.body()!!
            Log.d("PostRepository", "getComments response: comments=${body.comments.size}")
            body.comments
        }

    suspend fun addComment(postId: Int, content: String): NetworkResult<Comment> =
        safeApiCall {
            val resp = api.addComment(CreateCommentRequest(postId, content))
            if (!resp.isSuccessful) throw retrofit2.HttpException(resp)
            resp.body()!!.comment
        }
}
