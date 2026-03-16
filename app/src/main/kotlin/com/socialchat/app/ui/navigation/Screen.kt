package com.socialchat.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Explore : Screen("explore")
    object CreatePost : Screen("create_post")
    object ChatList : Screen("chat_list")
    object ChatRoom : Screen("chat_room/{roomId}") {
        fun createRoute(roomId: Int) = "chat_room/$roomId"
    }
    object Profile : Screen("profile")
    object UserProfile : Screen("user_profile/{username}") {
        fun createRoute(username: String) = "user_profile/$username"
    }
    object EditProfile : Screen("edit_profile")
    object FriendRequests : Screen("friend_requests")
    object Settings : Screen("settings")
}
