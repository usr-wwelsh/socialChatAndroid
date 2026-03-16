package com.socialchat.app.data.dto

import com.socialchat.app.data.model.ChatMessage
import com.socialchat.app.data.model.Chatroom

data class ChatroomsResponse(
    val chatrooms: List<Chatroom>? = null
)

data class MessagesResponse(
    val messages: List<ChatMessage>? = null
)
