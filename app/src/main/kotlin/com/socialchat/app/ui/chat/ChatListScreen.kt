package com.socialchat.app.ui.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
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
import com.socialchat.app.ui.dm.DmListScreen
import com.socialchat.app.ui.theme.*

private enum class ChatTab { GROUPS, DIRECT_MESSAGES }

@Composable
fun ChatListScreen(
    onNavigateToRoom: (Int) -> Unit,
    onNavigateToDm: (DmConversation) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    var selectedTab by remember { mutableStateOf(ChatTab.GROUPS) }

    LaunchedEffect(Unit) { viewModel.loadRooms() }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Tab row: Groups vs Direct Messages
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = Border, shape = RectangleShape)
        ) {
            TabButton(
                label = "Groups",
                icon = {
                    Icon(
                        Icons.Default.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (selectedTab == ChatTab.GROUPS) TextPrimary else TextSecondary
                    )
                },
                selected = selectedTab == ChatTab.GROUPS,
                modifier = Modifier.weight(1f)
            ) { selectedTab = ChatTab.GROUPS }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .border(1.dp, Border, RectangleShape)
            )

            TabButton(
                label = "Direct Messages",
                icon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (selectedTab == ChatTab.DIRECT_MESSAGES) Accent else TextSecondary
                    )
                },
                selected = selectedTab == ChatTab.DIRECT_MESSAGES,
                modifier = Modifier.weight(1f)
            ) { selectedTab = ChatTab.DIRECT_MESSAGES }
        }

        when (selectedTab) {
            ChatTab.GROUPS -> GroupsTab(listState, onNavigateToRoom)
            ChatTab.DIRECT_MESSAGES -> DmListScreen(onNavigateToConversation = onNavigateToDm)
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (selected) TextPrimary else TextSecondary,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .then(if (selected) Modifier.border(2.dp, Accent, RectangleShape) else Modifier)
        )
    }
}

@Composable
private fun GroupsTab(
    listState: ChatListUiState,
    onNavigateToRoom: (Int) -> Unit
) {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = "Group chat",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = room.name,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
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
