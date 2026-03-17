package com.socialchat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.socialchat.app.core.util.LocalBaseUrl
import com.socialchat.app.ui.theme.*

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
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(SecondaryBg)
                    .border(2.dp, Border, RectangleShape)
                    .then(
                        if (onMediaClick != null) Modifier.clickable { onMediaClick(fullUrl, mediaType) }
                        else Modifier
                    )
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = "Play video", tint = Accent, modifier = Modifier.size(64.dp))
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
