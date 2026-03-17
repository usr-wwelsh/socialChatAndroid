package com.socialchat.app.ui.components

import android.util.Patterns
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.socialchat.app.core.util.DateUtils
import com.socialchat.app.data.model.Post
import com.socialchat.app.ui.theme.*

@Composable
fun PostCard(
    post: Post,
    onLikeClick: (Post) -> Unit,
    onCommentClick: (Post) -> Unit,
    onUsernameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CardBg,
        shape = RectangleShape,
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Border, RectangleShape)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                UserAvatar(
                    profilePicture = post.profilePicture,
                    username = post.username ?: "",
                    size = 40.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.username ?: "",
                        color = Accent,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable { onUsernameClick(post.username ?: "") }
                    )
                    Text(
                        text = DateUtils.relativeTime(post.createdAt),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (!post.visibility.isNullOrEmpty() && post.visibility != "public") {
                    Text(
                        text = post.visibility,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Content with clickable links
            val uriHandler = LocalUriHandler.current
            val contentText = post.content ?: ""
            val annotatedContent = buildAnnotatedString {
                append(contentText)
                val matcher = Patterns.WEB_URL.matcher(contentText)
                while (matcher.find()) {
                    addStyle(
                        style = SpanStyle(color = Accent, textDecoration = TextDecoration.Underline),
                        start = matcher.start(),
                        end = matcher.end()
                    )
                    addStringAnnotation(
                        tag = "URL",
                        annotation = matcher.group(),
                        start = matcher.start(),
                        end = matcher.end()
                    )
                }
            }
            ClickableText(
                text = annotatedContent,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                onClick = { offset ->
                    annotatedContent.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { uriHandler.openUri(it.item) }
                }
            )

            // Media
            PostMediaContent(
                mediaType = post.mediaType,
                mediaUrl = post.mediaUrl
            )

            // Tags
            if (!post.tags.isNullOrEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(post.tags) { tag ->
                        TagChip(name = tag.name)
                    }
                }
            }

            // Reactions
            ReactionBar(
                likeCount = post.likeCount,
                commentCount = post.commentCount,
                isLiked = post.isLiked,
                onLikeClick = { onLikeClick(post) },
                onCommentClick = { onCommentClick(post) }
            )
        }
    }
}
