package com.socialchat.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.socialchat.app.core.util.LocalBaseUrl
import com.socialchat.app.ui.components.*
import com.socialchat.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigateToProfile: (String) -> Unit,
    onNavigateToReel: (Int) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val baseUrl = LocalBaseUrl.current

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

        if (uiState.query.isBlank()) {
            // Default: trending tags + media grid
            if (uiState.trendingTags.isNotEmpty()) {
                Text("Trending", color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(uiState.trendingTags) { tag ->
                        TagChip(name = tag.name, onClick = { viewModel.searchByTag(tag.name) })
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(uiState.mediaPosts, key = { _, post -> post.id }) { index, post ->
                    viewModel.loadMoreMediaIfNeeded(index)
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                MediaReelCache.posts = uiState.mediaPosts
                                onNavigateToReel(index)
                            }
                    ) {
                        when {
                            post.mediaType?.startsWith("image") == true -> {
                                val fullUrl = post.mediaUrl?.let { if (it.startsWith("http")) it else "$baseUrl$it" }
                                AsyncImage(
                                    model = fullUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            post.mediaType?.startsWith("video") == true -> {
                                val fullUrl = post.mediaUrl?.let { if (it.startsWith("http")) it else "$baseUrl$it" }
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = fullUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.align(Alignment.Center).size(36.dp)
                                    )
                                }
                            }
                            post.mediaType?.startsWith("audio") == true -> {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A2E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = "Audio", tint = Accent, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }
                if (uiState.isLoadingMedia) {
                    item {
                        Box(
                            modifier = Modifier.aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Accent, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
            } // end PullToRefreshBox
        } else {
            // Search results
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
                            onLikeClick = viewModel::toggleLike,
                            onCommentClick = {},
                            onUsernameClick = onNavigateToProfile
                        )
                    }
                }
            }
        }
    }
}
