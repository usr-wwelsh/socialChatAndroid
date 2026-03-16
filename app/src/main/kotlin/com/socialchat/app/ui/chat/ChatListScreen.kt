package com.socialchat.app.ui.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.core.util.DateUtils
import com.socialchat.app.ui.components.ErrorBanner
import com.socialchat.app.ui.components.LoadingSpinner
import com.socialchat.app.ui.theme.*

@Composable
fun ChatListScreen(
    onNavigateToRoom: (Int) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadRooms() }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            modifier = Modifier.padding(16.dp)
        )

        listState.error?.let { ErrorBanner(it, modifier = Modifier.padding(horizontal = 16.dp)) }

        if (listState.isLoading) {
            LoadingSpinner()
        } else {
            LazyColumn {
                items(listState.rooms, key = { it.id }) { room ->
                    Surface(
                        color = CardBg,
                        shape = RectangleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = Border, shape = RectangleShape)
                            .clickable { onNavigateToRoom(room.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = room.name,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = DateUtils.relativeTime(room.lastMessageTime),
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (!room.lastMessage.isNullOrEmpty()) {
                                Text(
                                    text = room.lastMessage,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Divider(color = Border, thickness = 1.dp)
                }
            }
        }
    }
}
