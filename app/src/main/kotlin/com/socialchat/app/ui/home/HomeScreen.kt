package com.socialchat.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.socialchat.app.ui.components.*
import com.socialchat.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToProfile: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val feedState by viewModel.feedState.collectAsState()
    val detailState by viewModel.detailState.collectAsState()
    val listState = rememberLazyListState()

    // Infinite scroll trigger
    val lastVisibleIndex by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
    }
    LaunchedEffect(lastVisibleIndex) {
        viewModel.loadMoreIfNeeded(lastVisibleIndex)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(feedState.isRefreshing),
            onRefresh = { viewModel.loadFeed(refresh = true) }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (feedState.error != null && posts.isEmpty()) {
                    item { ErrorBanner(feedState.error!!, modifier = Modifier.padding(16.dp)) }
                }

                itemsIndexed(posts, key = { _, post -> post.id }) { _, post ->
                    PostCard(
                        post = post,
                        onLikeClick = { viewModel.toggleLike(it) },
                        onCommentClick = { viewModel.openPostDetail(it) },
                        onUsernameClick = onNavigateToProfile,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                if (feedState.isLoading && posts.isNotEmpty()) {
                    item { LoadingSpinner() }
                }
                if (!feedState.hasMore && posts.isNotEmpty()) {
                    item {
                        Text(
                            "You're all caught up",
                            color = TextSecondary,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        if (feedState.isLoading && posts.isEmpty()) {
            LoadingSpinner(modifier = Modifier.fillMaxSize())
        }
    }

    // Post detail bottom sheet
    detailState.selectedPost?.let { post ->
        PostDetailSheet(
            post = post,
            comments = detailState.comments,
            commentsLoading = detailState.commentsLoading,
            commentsError = detailState.commentsError,
            commentInput = detailState.commentInput,
            onCommentInputChange = { viewModel.updateCommentInput(it) },
            onSubmitComment = { viewModel.submitComment() },
            onDismiss = { viewModel.closePostDetail() },
            onLikeClick = { viewModel.toggleLike(it) },
            onUsernameClick = onNavigateToProfile
        )
    }
}
