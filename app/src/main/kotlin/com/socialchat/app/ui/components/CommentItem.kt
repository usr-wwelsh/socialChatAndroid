package com.socialchat.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.socialchat.app.core.util.DateUtils
import com.socialchat.app.data.model.Comment
import com.socialchat.app.ui.theme.*

@Composable
fun CommentItem(comment: Comment, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UserAvatar(profilePicture = comment.profilePicture, username = comment.username, size = 32.dp)
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(comment.username, color = Accent, style = MaterialTheme.typography.bodySmall)
                Text(DateUtils.relativeTime(comment.createdAt), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Text(comment.content, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
