package com.socialchat.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.socialchat.app.ui.theme.Accent
import com.socialchat.app.ui.theme.TextSecondary
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme

@Composable
fun TypingIndicator(typingUsers: Set<String>, modifier: Modifier = Modifier) {
    if (typingUsers.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot"
    )

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val label = if (typingUsers.size == 1)
            "${typingUsers.first()} is typing"
        else
            "${typingUsers.size} people are typing"
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        repeat(3) { i ->
            Box(
                Modifier
                    .size(6.dp)
                    .alpha(if (i == 0) alpha else if (i == 1) (alpha + 0.3f).coerceIn(0f, 1f) else 1f - alpha + 0.3f)
                    .background(Accent, CircleShape)
            )
        }
    }
}
