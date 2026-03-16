package com.socialchat.app.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
fun ExploreScreen(
    onNavigateToProfile: (String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.onQueryChanged(it) },
            placeholder = { Text("Search users or #tags...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(Modifier.height(12.dp))

        if (uiState.trendingTags.isNotEmpty() && uiState.query.isBlank()) {
            Text("Trending", color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(uiState.trendingTags) { tag ->
                    TagChip(name = tag.name, onClick = { viewModel.searchByTag(tag.name) })
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        uiState.error?.let { ErrorBanner(it) }

        if (uiState.isLoading) {
            LoadingSpinner()
        } else if (uiState.userResults.isNotEmpty()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(uiState.userResults, key = { it.id }) { user ->
                    Surface(
                        color = CardBg,
                        shape = RectangleShape,
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToProfile(user.username) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            UserAvatar(profilePicture = user.profilePicture, username = user.username, size = 40.dp)
                            Column {
                                Text(user.username, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                                if (!user.bio.isNullOrEmpty()) {
                                    Text(user.bio, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(uiState.postResults, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        onLikeClick = {},
                        onCommentClick = {},
                        onUsernameClick = onNavigateToProfile
                    )
                }
            }
        }
    }
}
