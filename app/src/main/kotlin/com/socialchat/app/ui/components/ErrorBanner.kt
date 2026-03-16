package com.socialchat.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.socialchat.app.ui.theme.*

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Surface(
        color = ErrorRed.copy(alpha = 0.15f),
        shape = RectangleShape,
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, ErrorRed, RectangleShape)
    ) {
        Text(
            text = message,
            color = ErrorRed,
            modifier = Modifier.padding(12.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
    }
}
