package com.socialchat.app.ui.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.ui.components.ChatMessageBubble
import com.socialchat.app.ui.components.LoadingSpinner
import com.socialchat.app.ui.components.TypingIndicator
import com.socialchat.app.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ChatRoomScreen(
    roomId: Int,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val roomState by viewModel.roomState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentUsername by remember { mutableStateOf("") }

    // Get current username for bubble alignment
    LaunchedEffect(Unit) {
        // username loaded from ViewModel's prefs
    }

    LaunchedEffect(roomId) {
        viewModel.joinRoom(roomId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.leaveRoom() }
    }

    LaunchedEffect(roomState.messages.size) {
        if (roomState.messages.isNotEmpty()) {
            listState.animateScrollToItem(roomState.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 2.dp, color = Border, shape = RectangleShape)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Icon(
                imageVector = Icons.Default.Forum,
                contentDescription = "Group chat",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text("Group Chat", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text("Public", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }

        // Messages
        if (roomState.isLoading) {
            LoadingSpinner(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(roomState.messages, key = { "${it.id}_${it.createdAt}" }) { msg ->
                    ChatMessageBubble(
                        message = msg,
                        isOwn = msg.username == currentUsername
                    )
                }
            }
        }

        // Typing indicator
        TypingIndicator(typingUsers = roomState.typingUsers)

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 2.dp, color = Border, shape = RectangleShape)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = roomState.input,
                onValueChange = { viewModel.updateInput(it) },
                placeholder = { Text("Message...", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RectangleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Accent,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg
                )
            )
            IconButton(
                onClick = { viewModel.sendMessage() },
                enabled = roomState.input.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Accent)
            }
        }
    }
}
