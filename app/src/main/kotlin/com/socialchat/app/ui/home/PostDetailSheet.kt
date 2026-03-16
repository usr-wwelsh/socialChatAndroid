package com.socialchat.app.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.socialchat.app.data.model.Comment
import com.socialchat.app.data.model.Post
import com.socialchat.app.ui.components.*
import com.socialchat.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailSheet(
    post: Post,
    comments: List<Comment>,
    commentsLoading: Boolean,
    commentsError: String? = null,
    commentInput: String,
    onCommentInputChange: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onDismiss: () -> Unit,
    onLikeClick: (Post) -> Unit,
    onUsernameClick: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SecondaryBg,
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            PostCard(
                post = post,
                onLikeClick = onLikeClick,
                onCommentClick = {},
                onUsernameClick = onUsernameClick
            )

            Spacer(Modifier.height(16.dp))
            Divider(color = Border)
            Spacer(Modifier.height(8.dp))

            Text("Comments", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (commentsLoading) {
                LoadingSpinner()
            } else if (commentsError != null) {
                Text("Error loading comments: $commentsError", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (comments.isEmpty()) {
                        Text("No comments yet.", color = TextSecondary, modifier = Modifier.padding(8.dp))
                    }
                    comments.forEach { comment ->
                        CommentItem(comment = comment)
                        Divider(color = Border.copy(alpha = 0.5f))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Comment input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Border, RectangleShape)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = onCommentInputChange,
                    placeholder = { Text("Write a comment...", color = TextSecondary) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Accent,
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg
                    ),
                    shape = RectangleShape,
                    singleLine = true
                )
                IconButton(
                    onClick = onSubmitComment,
                    enabled = commentInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Accent)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
