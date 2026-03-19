package com.socialchat.app.ui.dm

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.core.util.DateUtils
import com.socialchat.app.data.model.DmConversation
import com.socialchat.app.ui.components.LoadingSpinner
import com.socialchat.app.ui.theme.*

@Composable
fun DmConversationScreen(
    conversation: DmConversation,
    onBack: () -> Unit,
    viewModel: DmViewModel = hiltViewModel()
) {
    val convState by viewModel.convState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(conversation) {
        viewModel.openConversation(conversation)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.leaveConversation() }
    }

    LaunchedEffect(convState.messages.size) {
        if (convState.messages.isNotEmpty()) {
            listState.animateScrollToItem(convState.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Border, RectangleShape)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Encrypted",
                tint = Accent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = conversation.partnerUsername,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "End-to-end encrypted",
                    color = Accent,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Password unlock prompt — shown when private key isn't in DataStore yet
        if (convState.needsPasswordUnlock) {
            PasswordUnlockBanner(
                isUnlocking = convState.isUnlocking,
                error = convState.unlockError,
                onUnlock = { password -> viewModel.unlockWithPassword(password) }
            )
        }

        // Messages
        if (convState.isLoading) {
            LoadingSpinner(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(convState.messages, key = { "${it.id}_${it.createdAt}" }) { msg ->
                    // In a 2-person DM: if sender is not the partner, the message is ours
                    val isOwn = convState.conversation?.let { msg.senderId != it.partnerId } ?: false
                    DmMessageBubble(message = msg, isOwn = isOwn)
                }
            }
        }

        convState.error?.let {
            Text(
                text = it,
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Border, RectangleShape)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = convState.input,
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
                enabled = convState.input.isNotBlank() && convState.cryptoReady
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Accent)
            }
        }
    }
}

@Composable
private fun PasswordUnlockBanner(
    isUnlocking: Boolean,
    error: String?,
    onUnlock: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    Surface(
        color = SecondaryBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Accent.copy(alpha = 0.4f), RectangleShape)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Accent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Enter your password to decrypt messages",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = TextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
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
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onUnlock(password) },
                    enabled = password.isNotBlank() && !isUnlocking,
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    if (isUnlocking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Unlock", color = TextPrimary)
                    }
                }
            }
            if (error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = error, color = ErrorRed, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DmMessageBubble(
    message: com.socialchat.app.data.model.DmMessage,
    isOwn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
    ) {
        if (!isOwn) {
            Text(
                text = message.senderUsername,
                color = Accent,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Surface(
            color = if (isOwn) Accent.copy(alpha = 0.2f) else CardBg,
            shape = RectangleShape,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .border(1.dp, if (isOwn) Accent.copy(alpha = 0.5f) else Border, RectangleShape)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = message.content.ifEmpty { "[encrypted]" },
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = DateUtils.relativeTime(message.createdAt),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                )
            }
        }
    }
}
