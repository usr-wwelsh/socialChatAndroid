package com.socialchat.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialchat.app.ui.theme.*

@Composable
fun ReactionBar(
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onLikeClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) ErrorRed else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(text = "$likeCount", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onCommentClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Comment",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(text = "$commentCount", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
