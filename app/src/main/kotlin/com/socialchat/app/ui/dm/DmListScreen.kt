package com.socialchat.app.ui.dm

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.core.util.DateUtils
import com.socialchat.app.data.model.DmConversation
import com.socialchat.app.ui.components.ErrorBanner
import com.socialchat.app.ui.components.LoadingSpinner
import com.socialchat.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DmListScreen(
    onNavigateToConversation: (DmConversation) -> Unit,
    viewModel: DmViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadConversations() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            listState.error?.let { ErrorBanner(it, modifier = Modifier.padding(horizontal = 16.dp)) }

            if (listState.isLoading || listState.startingConversation) {
                LoadingSpinner()
            } else if (listState.conversations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No direct messages yet.\nTap + to message a friend.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(listState.conversations, key = { it.id }) { conv ->
                        Surface(
                            color = CardBg,
                            shape = RectangleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Border, RectangleShape)
                                .clickable { onNavigateToConversation(conv) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted DM",
                                    tint = Accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conv.partnerUsername,
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = DateUtils.relativeTime(conv.updatedAt),
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        text = "Encrypted message",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = Border, thickness = 1.dp)
                    }
                }
            }
        }

        // New DM button
        FloatingActionButton(
            onClick = { viewModel.openFriendPicker() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Accent,
            shape = RectangleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "New DM", tint = TextPrimary)
        }
    }

    // Friend picker bottom sheet
    if (listState.showFriendPicker) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeFriendPicker() },
            containerColor = SecondaryBg,
            dragHandle = null
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Direct Message",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { viewModel.closeFriendPicker() }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
                HorizontalDivider(color = Border, thickness = 1.dp)

                if (listState.friendsLoading) {
                    LoadingSpinner(modifier = Modifier.padding(vertical = 32.dp))
                } else if (listState.friends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No friends yet", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        items(listState.friends, key = { it.id }) { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.startConversationWith(friend, onNavigateToConversation)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = friend.username,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            HorizontalDivider(color = Border, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}
