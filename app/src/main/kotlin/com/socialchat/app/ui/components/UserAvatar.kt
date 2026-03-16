package com.socialchat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.socialchat.app.ui.theme.*

@Composable
fun UserAvatar(
    profilePicture: String?,
    username: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val imageModel = if (!profilePicture.isNullOrEmpty() && !profilePicture.startsWith("data:")) {
        "data:image/jpeg;base64,$profilePicture"
    } else {
        profilePicture
    }

    if (!imageModel.isNullOrEmpty()) {
        AsyncImage(
            model = imageModel,
            contentDescription = "$username avatar",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(2.dp, Border, CircleShape)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(SecondaryBg)
                .border(2.dp, Border, CircleShape)
        ) {
            Text(
                text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = Accent,
                fontSize = (size.value * 0.4f).sp
            )
        }
    }
}
