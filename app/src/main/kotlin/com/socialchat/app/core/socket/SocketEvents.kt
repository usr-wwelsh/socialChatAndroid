package com.socialchat.app.core.socket

object SocketEvents {
    const val CONNECT = "connect"
    const val DISCONNECT = "disconnect"
    const val JOIN_ROOM = "join_chatroom"
    const val LEAVE_ROOM = "leave_chatroom"
    const val SEND_MESSAGE = "send_message"
    const val NEW_MESSAGE = "new_message"
    const val TYPING = "typing"
    const val STOP_TYPING = "stop_typing"
    const val USER_TYPING = "user_typing"
    const val USER_STOP_TYPING = "user_stop_typing"
    // DM events
    const val JOIN_DM = "join_dm"
    const val LEAVE_DM = "leave_dm"
    const val NEW_DM = "new_dm"
    const val DM_NOTIFICATION = "dm_notification"
}
