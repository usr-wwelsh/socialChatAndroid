package com.socialchat.app.ui.friends

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.ui.components.*
import com.socialchat.app.ui.theme.*

@Composable
fun FriendRequestsScreen(
    onBack: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            Text("Friend Requests", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }

        uiState.error?.let { ErrorBanner(it, modifier = Modifier.padding(16.dp)) }

        if (uiState.isLoading) {
            LoadingSpinner()
        } else if (uiState.requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pending friend requests", color = TextSecondary)
            }
        } else {
            LazyColumn {
                items(uiState.requests, key = { it.id }) { request ->
                    Surface(
                        color = CardBg,
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Border, RectangleShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            UserAvatar(
                                profilePicture = request.requester?.profilePicture,
                                username = request.requester?.username ?: "?",
                                size = 48.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    request.requester?.username ?: "Unknown",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("wants to be your friend", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BrutalistButton(
                                    text = "Accept",
                                    onClick = { viewModel.acceptRequest(request.id) },
                                    modifier = Modifier.width(90.dp)
                                )
                                BrutalistButton(
                                    text = "Reject",
                                    onClick = { viewModel.rejectRequest(request.id) },
                                    outlined = true,
                                    modifier = Modifier.width(90.dp)
                                )
                            }
                        }
                    }
                    Divider(color = Border)
                }
            }
        }
    }
}
