package com.socialchat.app.ui.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.socialchat.app.ui.components.*
import com.socialchat.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showVisibilityMenu by remember { mutableStateOf(false) }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.setMedia(context, it) }
    }

    LaunchedEffect(uiState.posted) {
        if (uiState.posted) onPostCreated()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("New Post", style = MaterialTheme.typography.titleLarge, color = TextPrimary)

        uiState.error?.let { ErrorBanner(it) }

        OutlinedTextField(
            value = uiState.content,
            onValueChange = { viewModel.updateContent(it) },
            placeholder = { Text("What's on your mind?", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            singleLine = false,
            maxLines = 6,
            shape = RectangleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Accent,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg
            )
        )

        // Media picker
        if (uiState.mediaData != null && uiState.mediaType?.startsWith("image") == true) {
            Box {
                AsyncImage(
                    model = uiState.mediaData,
                    contentDescription = "Selected media",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .border(2.dp, Border, RectangleShape)
                )
                IconButton(
                    onClick = { viewModel.clearMedia() },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove media", tint = ErrorRed)
                }
            }
        } else if (uiState.mediaUri != null) {
            Row(
                modifier = Modifier.border(2.dp, Border, RectangleShape).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Media attached: ${uiState.mediaType}", color = TextSecondary)
                IconButton(onClick = { viewModel.clearMedia() }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = ErrorRed)
                }
            }
        } else {
            BrutalistButton(
                text = "Attach Media",
                onClick = {
                    mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                },
                outlined = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tags
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.tagInput,
                onValueChange = { viewModel.updateTagInput(it) },
                placeholder = { Text("Add tag...", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RectangleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Accent,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg
                )
            )
            BrutalistButton(
                text = "Add",
                onClick = { viewModel.addTag(uiState.tagInput) },
                outlined = true,
                modifier = Modifier.width(72.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            uiState.tags.forEach { tag ->
                InputChip(
                    selected = false,
                    onClick = { viewModel.removeTag(tag) },
                    label = { Text("#$tag", color = Accent) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp), tint = TextSecondary) },
                    shape = RectangleShape,
                    border = InputChipDefaults.inputChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = Border,
                        selectedBorderColor = Accent
                    ),
                    colors = InputChipDefaults.inputChipColors(containerColor = SecondaryBg)
                )
            }
        }

        // Visibility
        Box {
            BrutalistButton(
                text = "Visibility: ${uiState.visibility}",
                onClick = { showVisibilityMenu = true },
                outlined = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = showVisibilityMenu,
                onDismissRequest = { showVisibilityMenu = false }
            ) {
                listOf("public", "friends", "private").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = TextPrimary) },
                        onClick = {
                            viewModel.updateVisibility(option)
                            showVisibilityMenu = false
                        }
                    )
                }
            }
        }

        BrutalistButton(
            text = "POST",
            onClick = { viewModel.submitPost() },
            isLoading = uiState.isLoading,
            enabled = uiState.content.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
