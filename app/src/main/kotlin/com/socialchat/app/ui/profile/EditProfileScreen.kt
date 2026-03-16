package com.socialchat.app.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.ui.components.*
import com.socialchat.app.ui.theme.*

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.setEditAvatar(context, it) }
    }

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text("Edit Profile", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }

        uiState.error?.let { ErrorBanner(it) }

        // Avatar
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            UserAvatar(
                profilePicture = uiState.editAvatarData ?: uiState.user?.profilePicture,
                username = uiState.user?.username ?: "",
                size = 80.dp
            )
        }

        BrutalistButton(
            text = "Change Avatar",
            onClick = {
                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            outlined = true,
            modifier = Modifier.fillMaxWidth()
        )

        BrutalistTextField(
            value = uiState.editBio,
            onValueChange = { viewModel.updateEditBio(it) },
            label = "Bio",
            singleLine = false,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )

        BrutalistButton(
            text = "SAVE",
            onClick = { viewModel.saveProfile() },
            isLoading = uiState.isUpdating,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
