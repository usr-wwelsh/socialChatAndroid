package com.socialchat.app.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.ui.components.BrutalistButton
import com.socialchat.app.ui.components.BrutalistTextField
import com.socialchat.app.ui.components.ConfirmDialog
import com.socialchat.app.ui.components.ErrorBanner
import com.socialchat.app.ui.theme.*

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text("Settings", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }

        uiState.error?.let { ErrorBanner(it) }
        if (uiState.savedSuccess) {
            Surface(
                color = Success.copy(alpha = 0.15f),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().border(2.dp, Success, RectangleShape)
            ) {
                Text("Saved!", color = Success, modifier = Modifier.padding(12.dp))
            }
        }

        Text("Server URL", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        BrutalistTextField(
            value = uiState.baseUrl,
            onValueChange = { viewModel.updateBaseUrl(it) },
            label = "Base URL",
            placeholder = "https://chat.wwel.sh",
            modifier = Modifier.fillMaxWidth()
        )
        BrutalistButton(
            text = "SAVE URL",
            onClick = { viewModel.saveBaseUrl() },
            isLoading = uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        Divider(color = Border)

        BrutalistButton(
            text = "LOG OUT",
            onClick = { showLogoutDialog = true },
            outlined = true,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Log Out",
            message = "Are you sure you want to log out?",
            confirmText = "Log Out",
            onConfirm = { viewModel.logout(onLogout) },
            onDismiss = { showLogoutDialog = false }
        )
    }
}
