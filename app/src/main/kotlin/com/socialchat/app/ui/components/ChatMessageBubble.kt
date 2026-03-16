package com.socialchat.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.socialchat.app.core.util.DateUtils
import com.socialchat.app.data.model.ChatMessage
import com.socialchat.app.ui.theme.*

@Composable
fun ChatMessageBubble(message: ChatMessage, isOwn: Boolean, modifier: Modifier = Modifier) {
    val alignment = if (isOwn) Alignment.End else Alignment.Start
    val bgColor = if (isOwn) Accent.copy(alpha = 0.2f) else SecondaryBg

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        if (!isOwn) {
            Text(
                text = message.username,
                color = Accent,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        Surface(
            color = bgColor,
            shape = RectangleShape,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .border(1.dp, if (isOwn) Accent.copy(alpha = 0.5f) else Border, RectangleShape)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(message.content, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = DateUtils.relativeTime(message.createdAt),
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                )
            }
        }
    }
}
