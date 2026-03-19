package com.socialchat.app.core.socket

import android.util.Log
import com.google.gson.Gson
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.data.model.ChatMessage
import com.socialchat.app.data.model.DmMessage
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val prefs: UserPreferences
) {
    private var socket: Socket? = null
    private val gson = Gson()
    private val tag = "SocketManager"
    private var currentRoomId: Int = -1

    private val _newMessages = MutableSharedFlow<ChatMessage>(replay = 0, extraBufferCapacity = 64)
    val newMessages: Flow<ChatMessage> = _newMessages.asSharedFlow()

    private val _typingEvents = MutableSharedFlow<Pair<Int, String>>(replay = 0, extraBufferCapacity = 32)
    val typingEvents: Flow<Pair<Int, String>> = _typingEvents.asSharedFlow()

    private val _stopTypingEvents = MutableSharedFlow<Pair<Int, String>>(replay = 0, extraBufferCapacity = 32)
    val stopTypingEvents: Flow<Pair<Int, String>> = _stopTypingEvents.asSharedFlow()

    private val _connectionState = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)
    val connectionState: Flow<Boolean> = _connectionState.asSharedFlow()

    // Raw DM message events — content is still encrypted; DmRepository decrypts
    private val _newDmMessages = MutableSharedFlow<DmMessage>(replay = 0, extraBufferCapacity = 64)
    val newDmMessages: Flow<DmMessage> = _newDmMessages.asSharedFlow()

    // Notification that a DM arrived (for badge updates) — pair of (conversationId, senderUsername)
    private val _dmNotifications = MutableSharedFlow<Pair<Int, String>>(replay = 0, extraBufferCapacity = 32)
    val dmNotifications: Flow<Pair<Int, String>> = _dmNotifications.asSharedFlow()

    fun connect() {
        if (socket?.connected() == true) return
        try {
            val baseUrl = runBlocking { prefs.getBaseUrl() }
            val cookie = runBlocking { prefs.getRawCookieHeader() }

            val options = IO.Options().apply {
                transports = arrayOf(WebSocket.NAME)
                if (cookie.isNotEmpty()) {
                    extraHeaders = mapOf("Cookie" to listOf(cookie))
                }
            }
            socket = IO.socket(URI.create(baseUrl), options)
            registerHandlers()
            socket?.connect()
        } catch (e: Exception) {
            Log.e(tag, "Failed to connect socket", e)
        }
    }

    private fun registerHandlers() {
        socket?.on(SocketEvents.CONNECT) {
            Log.d(tag, "Socket connected")
            _connectionState.tryEmit(true)
        }
        socket?.on(SocketEvents.DISCONNECT) {
            Log.d(tag, "Socket disconnected")
            _connectionState.tryEmit(false)
        }
        socket?.on(SocketEvents.NEW_MESSAGE) { args ->
            try {
                val json = args[0] as? JSONObject ?: return@on
                val msg = gson.fromJson(json.toString(), ChatMessage::class.java)
                _newMessages.tryEmit(msg)
            } catch (e: Exception) {
                Log.e(tag, "Error parsing message", e)
            }
        }
        // Backend emits user_typing/{user_stop_typing} to the room (no chatroomId in payload)
        // so we use currentRoomId to identify which room the event is for
        socket?.on(SocketEvents.USER_TYPING) { args ->
            try {
                val json = args[0] as? JSONObject ?: return@on
                val username = json.getString("username")
                _typingEvents.tryEmit(Pair(currentRoomId, username))
            } catch (_: Exception) {}
        }
        socket?.on(SocketEvents.USER_STOP_TYPING) { args ->
            try {
                val json = args[0] as? JSONObject ?: return@on
                val username = json.getString("username")
                _stopTypingEvents.tryEmit(Pair(currentRoomId, username))
            } catch (_: Exception) {}
        }
        socket?.on(SocketEvents.NEW_DM) { args ->
            try {
                val json = args[0] as? JSONObject ?: return@on
                val msg = DmMessage(
                    id = json.optInt("id"),
                    conversationId = json.optInt("conversation_id"),
                    senderId = json.optInt("sender_id"),
                    senderUsername = json.optString("sender_username"),
                    senderAvatar = if (json.has("sender_avatar") && !json.isNull("sender_avatar")) json.getString("sender_avatar") else null,
                    ciphertext = json.optString("ciphertext"),
                    iv = json.optString("iv"),
                    createdAt = if (json.has("created_at") && !json.isNull("created_at")) json.getString("created_at") else null
                )
                _newDmMessages.tryEmit(msg)
            } catch (e: Exception) {
                Log.e(tag, "Error parsing new_dm event", e)
            }
        }
        socket?.on(SocketEvents.DM_NOTIFICATION) { args ->
            try {
                val json = args[0] as? JSONObject ?: return@on
                val conversationId = json.optInt("conversationId")
                val senderUsername = json.optString("senderUsername")
                _dmNotifications.tryEmit(Pair(conversationId, senderUsername))
            } catch (_: Exception) {}
        }
    }

    fun joinRoom(roomId: Int) {
        currentRoomId = roomId
        // Backend expects join_chatroom(chatroomId) as a bare integer
        socket?.emit(SocketEvents.JOIN_ROOM, roomId)
    }

    fun leaveRoom(roomId: Int) {
        if (currentRoomId == roomId) currentRoomId = -1
        socket?.emit(SocketEvents.LEAVE_ROOM, roomId)
    }

    fun sendMessage(roomId: Int, content: String) {
        socket?.emit(SocketEvents.SEND_MESSAGE, JSONObject().apply {
            put("chatroomId", roomId)
            put("message", content)
        })
    }

    fun sendTyping(roomId: Int) {
        socket?.emit(SocketEvents.TYPING, JSONObject().apply { put("chatroomId", roomId) })
    }

    fun sendStopTyping(roomId: Int) {
        socket?.emit(SocketEvents.STOP_TYPING, JSONObject().apply { put("chatroomId", roomId) })
    }

    fun joinDmConversation(conversationId: Int) {
        // Server expects bare integer, just like join_chatroom
        socket?.emit(SocketEvents.JOIN_DM, conversationId)
    }

    fun leaveDmConversation(conversationId: Int) {
        socket?.emit(SocketEvents.LEAVE_DM, conversationId)
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}
