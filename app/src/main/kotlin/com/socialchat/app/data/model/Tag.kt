package com.socialchat.app.data.model

data class Tag(
    val id: Int,
    val name: String,
    @com.google.gson.annotations.SerializedName("post_count") val postCount: Int = 0
)
