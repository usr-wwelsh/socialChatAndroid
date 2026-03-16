package com.socialchat.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.socialchat.app.ui.theme.*

@Composable
fun TagChip(
    name: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = SecondaryBg,
        shape = RectangleShape,
        modifier = modifier
            .border(1.dp, Accent, RectangleShape)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = "#$name",
            color = Accent,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
