package com.socialchat.app.ui.explore

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.socialchat.app.core.util.LocalBaseUrl
import com.socialchat.app.ui.components.UserAvatar
import com.socialchat.app.ui.home.PostDetailSheet
import com.socialchat.app.ui.theme.Accent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaReelScreen(
    startIndex: Int,
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: MediaReelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val posts = uiState.posts
    val baseUrl = LocalBaseUrl.current

    if (posts.isEmpty()) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val pagerState = rememberPagerState(initialPage = startIndex.coerceIn(0, posts.size - 1)) { posts.size }
    var showComments by remember { mutableStateOf(false) }
    val currentPost = posts[pagerState.currentPage]

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val post = posts[page]
            val isActive = pagerState.settledPage == page
            val fullUrl = post.mediaUrl?.let { if (it.startsWith("http")) it else "$baseUrl$it" }

            Box(modifier = Modifier.fillMaxSize()) {
                // Media content
                when {
                    post.mediaType?.startsWith("image") == true ->
                        AsyncImage(
                            model = fullUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    post.mediaType?.startsWith("video") == true ->
                        ReelVideoPlayer(url = fullUrl ?: "", isActive = isActive)
                    post.mediaType?.startsWith("audio") == true ->
                        ReelAudioPlayer(url = fullUrl ?: "", isActive = isActive)
                    else ->
                        Box(Modifier.fillMaxSize().background(Color(0xFF1A1A1A)))
                }

                // Bottom gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))
                        )
                )

                // Bottom-left: username + caption
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, end = 80.dp, bottom = 28.dp)
                ) {
                    Text(
                        "@${post.username ?: ""}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!post.content.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            post.content,
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right side: avatar + like + comment
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UserAvatar(
                        profilePicture = post.profilePicture,
                        username = post.username ?: "",
                        size = 44.dp,
                        modifier = Modifier.clickable { onNavigateToProfile(post.username ?: "") }
                    )

                    // Like button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { viewModel.toggleLike(post) }) {
                            Icon(
                                if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (post.isLiked) "Unlike" else "Like",
                                tint = if (post.isLiked) Color.Red else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(post.likeCount.toString(), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }

                    // Comment button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            viewModel.loadComments(post.id)
                            showComments = true
                        }) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(post.commentCount.toString(), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }

    if (showComments) {
        PostDetailSheet(
            post = currentPost,
            comments = uiState.comments,
            commentsLoading = uiState.isLoadingComments,
            commentsError = uiState.commentsError,
            commentInput = uiState.commentInput,
            onCommentInputChange = viewModel::onCommentInputChange,
            onSubmitComment = { viewModel.submitComment(currentPost.id) },
            onDismiss = { showComments = false },
            onLikeClick = viewModel::toggleLike,
            onUsernameClick = { username ->
                showComments = false
                onNavigateToProfile(username)
            }
        )
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun ReelVideoPlayer(url: String, isActive: Boolean) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            prepare()
        }
    }
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }
    LaunchedEffect(isActive) {
        if (isActive) exoPlayer.play() else exoPlayer.pause()
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ReelAudioPlayer(url: String, isActive: Boolean) {
    var isPlaying by remember { mutableStateOf(false) }
    var isReady by remember { mutableStateOf(false) }
    var mp by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(url) {
        val player = MediaPlayer()
        try {
            player.setDataSource(url)
            player.prepareAsync()
            player.setOnPreparedListener { isReady = true }
            player.setOnCompletionListener { isPlaying = false }
        } catch (_: Exception) {}
        mp = player
        onDispose {
            player.release()
            mp = null
            isPlaying = false
            isReady = false
        }
    }

    LaunchedEffect(isActive) {
        if (!isActive) {
            try { mp?.takeIf { it.isPlaying }?.pause() } catch (_: Exception) {}
            isPlaying = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF0D0D1A)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(96.dp)
            )
            IconButton(
                onClick = {
                    val player = mp ?: return@IconButton
                    if (!isReady) return@IconButton
                    if (isPlaying) {
                        try { player.pause() } catch (_: Exception) {}
                        isPlaying = false
                    } else {
                        try { player.start(); isPlaying = true } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}
