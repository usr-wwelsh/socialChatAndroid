package com.socialchat.app.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.ui.components.*
import com.socialchat.app.ui.theme.*

@Composable
fun UserProfileScreen(
    username: String,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(username) { viewModel.loadUserProfile(username) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Border, RectangleShape)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(username, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }

        if (uiState.isLoading) { LoadingSpinner(); return@Column }
        uiState.error?.let { ErrorBanner(it, modifier = Modifier.padding(16.dp)); return@Column }

        LazyColumn {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Border, RectangleShape)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserAvatar(uiState.user?.profilePicture, uiState.user?.username ?: username, size = 80.dp)
                    Text(uiState.user?.username ?: username, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
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

                    uiState.user?.let { user ->
                        when {
                            user.isFriend -> BrutalistButton(
                                text = "Remove Friend",
                                onClick = { viewModel.removeFriend() },
                                outlined = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            user.friendRequestSent -> BrutalistButton(
                                text = "Request Sent",
                                onClick = {},
                                outlined = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                            else -> BrutalistButton(
                                text = "Add Friend",
                                onClick = { viewModel.sendFriendRequest(user.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (uiState.friends.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.friends) { friend ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                UserAvatar(friend.profilePicture, friend.username, size = 40.dp)
                                Text(friend.username, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
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
        }
    }
}
