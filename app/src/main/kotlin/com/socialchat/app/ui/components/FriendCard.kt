package com.socialchat.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.socialchat.app.data.model.User
import com.socialchat.app.ui.theme.*

@Composable
fun FriendCard(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CardBg,
        shape = RectangleShape,
        modifier = modifier
            .border(2.dp, Border, RectangleShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(profilePicture = user.profilePicture, username = user.username, size = 48.dp)
            Column {
                Text(user.username, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                if (!user.bio.isNullOrEmpty()) {
                    Text(user.bio, color = TextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
        }
    }
}
