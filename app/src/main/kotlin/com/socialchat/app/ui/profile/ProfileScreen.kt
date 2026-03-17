package com.socialchat.app.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.ui.components.*
import com.socialchat.app.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToEdit: () -> Unit,
    onNavigateToFriendRequests: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Infinite scroll trigger
    val lastVisibleIndex by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
    }
    LaunchedEffect(lastVisibleIndex) {
        if (uiState.hasMore && lastVisibleIndex >= uiState.posts.size - 3) {
            viewModel.loadMorePosts()
        }
    }

    LaunchedEffect(Unit) { viewModel.loadMyProfile() }

    if (uiState.isLoading) { LoadingSpinner(Modifier.fillMaxSize()); return }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        item {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Border, RectangleShape)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onNavigateToFriendRequests) {
                        Icon(Icons.Default.PeopleAlt, contentDescription = "Friend Requests", tint = TextSecondary)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                }
                UserAvatar(
                    profilePicture = uiState.user?.profilePicture,
                    username = uiState.user?.username ?: "",
                    size = 80.dp
                )
                Text(uiState.user?.username ?: "", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                if (!uiState.user?.bio.isNullOrEmpty()) {
                    Text(uiState.user!!.bio!!, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
                if (!uiState.user?.links.isNullOrEmpty()) {
                    val uriHandler = LocalUriHandler.current
                    val parsedLinks = runCatching {
                        val obj = org.json.JSONObject(uiState.user!!.links!!)
                        obj.keys().asSequence().map { key -> key to obj.getString(key) }.toList()
                    }.getOrNull()
                    if (parsedLinks != null) {
                        parsedLinks.forEach { (label, url) ->
                            Text(
                                text = label,
                                color = Accent,
                                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                                modifier = Modifier.clickable {
                                    val fullUrl = if (url.startsWith("http")) url else "https://$url"
                                    uriHandler.openUri(fullUrl)
                                }
                            )
                        }
                    } else {
                        uiState.user!!.links!!.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.forEach { link ->
                            Text(
                                text = link,
                                color = Accent,
                                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                                modifier = Modifier.clickable {
                                    val url = if (link.startsWith("http")) link else "https://$link"
                                    uriHandler.openUri(url)
                                }
                            )
                        }
                    }
                }
                BrutalistButton(
                    text = "Edit Profile",
                    onClick = onNavigateToEdit,
                    outlined = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Friends strip
        if (uiState.friends.isNotEmpty()) {
            item {
                Text(
                    "Friends (${uiState.friends.size})",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uiState.friends) { friend ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            UserAvatar(friend.profilePicture, friend.username, size = 48.dp)
                            Text(friend.username, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Posts grid
        item {
            Text(
                "Posts",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
        }
        items(uiState.posts, key = { it.id }) { post ->
            PostCard(
                post = post,
                onLikeClick = {},
                onCommentClick = {},
                onUsernameClick = {},
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        if (uiState.hasMore) {
            item { LoadingSpinner() }
        }
    }
}
