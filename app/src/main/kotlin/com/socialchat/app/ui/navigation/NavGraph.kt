package com.socialchat.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.socialchat.app.ui.auth.AuthViewModel
import com.socialchat.app.ui.auth.LoginScreen
import com.socialchat.app.ui.auth.RegisterScreen
import com.socialchat.app.ui.chat.ChatListScreen
import com.socialchat.app.ui.chat.ChatRoomScreen
import com.socialchat.app.ui.createpost.CreatePostScreen
import com.socialchat.app.ui.dm.DmConversationCache
import com.socialchat.app.ui.dm.DmConversationScreen
import com.socialchat.app.ui.explore.ExploreScreen
import com.socialchat.app.ui.friends.FriendRequestsScreen
import com.socialchat.app.ui.home.HomeScreen
import com.socialchat.app.ui.profile.EditProfileScreen
import com.socialchat.app.ui.profile.ProfileScreen
import com.socialchat.app.ui.profile.UserProfileScreen
import com.socialchat.app.ui.settings.SettingsScreen
import com.socialchat.app.ui.theme.PrimaryBg

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val startDestination = if (authState.isAuthenticated) Screen.Home.route else Screen.Login.route

    val bottomNavRoutes = setOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.CreatePost.route,
        Screen.ChatList.route,
        Screen.Profile.route
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = PrimaryBg,
        bottomBar = {
            if (showBottomBar) BottomNavBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToProfile = { username ->
                        navController.navigate(Screen.UserProfile.createRoute(username))
                    }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    onNavigateToProfile = { username ->
                        navController.navigate(Screen.UserProfile.createRoute(username))
                    }
                )
            }
            composable(Screen.CreatePost.route) {
                CreatePostScreen(
                    onPostCreated = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) } }
                )
            }
            composable(Screen.ChatList.route) {
                ChatListScreen(
                    onNavigateToRoom = { roomId ->
                        navController.navigate(Screen.ChatRoom.createRoute(roomId))
                    },
                    onNavigateToDm = { conversation ->
                        DmConversationCache.current = conversation
                        navController.navigate(Screen.DmConversation.createRoute(conversation.id))
                    }
                )
            }
            composable(
                route = Screen.DmConversation.route,
                arguments = listOf(navArgument("conversationId") { type = NavType.IntType })
            ) {
                val conversation = DmConversationCache.current ?: return@composable
                DmConversationScreen(
                    conversation = conversation,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.ChatRoom.route,
                arguments = listOf(navArgument("roomId") { type = NavType.IntType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getInt("roomId") ?: return@composable
                ChatRoomScreen(
                    roomId = roomId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToFriendRequests = { navController.navigate(Screen.FriendRequests.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }
                )
            }
            composable(
                route = Screen.UserProfile.route,
                arguments = listOf(navArgument("username") { type = NavType.StringType })
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: return@composable
                UserProfileScreen(
                    username = username,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.FriendRequests.route) {
                FriendRequestsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
