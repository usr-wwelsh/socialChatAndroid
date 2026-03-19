package com.socialchat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.socialchat.app.core.util.LocalBaseUrl
import com.socialchat.app.ui.theme.*

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PostMediaContent(
    mediaType: String?,
    mediaUrl: String?,
    modifier: Modifier = Modifier,
    onMediaClick: ((url: String, type: String) -> Unit)? = null
) {
    if (mediaType == null || mediaUrl == null) return

    val baseUrl = LocalBaseUrl.current
    val fullUrl = if (mediaUrl.startsWith("http")) mediaUrl else "$baseUrl$mediaUrl"

    var isPlayingVideo by remember { mutableStateOf(false) }

    when {
        mediaType.startsWith("image") -> {
            AsyncImage(
                model = fullUrl,
                contentDescription = "Post image",
                contentScale = ContentScale.FillWidth,
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .border(2.dp, Border, RectangleShape)
                    .then(
                        if (onMediaClick != null) Modifier.clickable { onMediaClick(fullUrl, mediaType) }
                        else Modifier
                    )
            )
        }

        mediaType.startsWith("video") -> {
            if (isPlayingVideo) {
                val context = LocalContext.current
                val exoPlayer = remember(fullUrl) {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(fullUrl))
                        repeatMode = ExoPlayer.REPEAT_MODE_ONE
                        playWhenReady = true
                        prepare()
                    }
                }
                DisposableEffect(exoPlayer) {
                    onDispose { exoPlayer.release() }
                }
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .border(2.dp, Border, RectangleShape)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(SecondaryBg)
                        .border(2.dp, Border, RectangleShape)
                        .clickable { isPlayingVideo = true }
                ) {
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = "Video thumbnail",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "Play video",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        mediaType.startsWith("audio") -> {
            Surface(
                color = SecondaryBg,
                shape = RectangleShape,
                modifier = modifier
                    .fillMaxWidth()
                    .border(2.dp, Border, RectangleShape)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.AudioFile, contentDescription = "Audio", tint = Accent)
                    Text("Audio attachment", color = TextSecondary)
                }
            }
        }
    }
}
