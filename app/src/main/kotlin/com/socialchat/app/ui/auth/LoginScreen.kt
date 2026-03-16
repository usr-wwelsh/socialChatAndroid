package com.socialchat.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.ui.components.BrutalistButton
import com.socialchat.app.ui.components.BrutalistTextField
import com.socialchat.app.ui.components.ErrorBanner
import com.socialchat.app.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var serverDropdownExpanded by remember { mutableStateOf(false) }
    var showAddServerDialog by remember { mutableStateOf(false) }
    var newServerUrl by remember { mutableStateOf("") }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = SecondaryBg,
            shape = RectangleShape,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Border, RectangleShape)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "1socialChat",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Accent
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )

                // Server selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { serverDropdownExpanded = true },
                        color = CardBg,
                        shape = RectangleShape,
                        border = BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = authState.selectedServer.ifEmpty { UserPreferences.DEFAULT_BASE_URL },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = serverDropdownExpanded,
                        onDismissRequest = { serverDropdownExpanded = false },
                        modifier = Modifier
                            .background(SecondaryBg)
                            .border(1.dp, Border, RectangleShape)
                    ) {
                        authState.serverList.forEach { server ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = server,
                                        color = if (server == authState.selectedServer) Accent else TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                onClick = {
                                    viewModel.selectServer(server)
                                    serverDropdownExpanded = false
                                },
                                trailingIcon = if (server != UserPreferences.DEFAULT_BASE_URL) ({
                                    IconButton(
                                        onClick = {
                                            viewModel.removeServer(server)
                                            serverDropdownExpanded = false
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove server",
                                            tint = TextSecondary
                                        )
                                    }
                                }) else null
                            )
                        }
                        HorizontalDivider(color = Border)
                        DropdownMenuItem(
                            text = { Text("Add Server...", color = Accent) },
                            onClick = {
                                serverDropdownExpanded = false
                                showAddServerDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Accent
                                )
                            }
                        )
                    }
                }

                authState.error?.let { ErrorBanner(it) }

                BrutalistTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    modifier = Modifier.fillMaxWidth()
                )
                BrutalistTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                BrutalistButton(
                    text = "SIGN IN",
                    onClick = { viewModel.login(username, password) },
                    isLoading = authState.isLoading,
                    enabled = username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text("Don't have an account? Register", color = Accent)
                }
            }
        }
    }

    if (showAddServerDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddServerDialog = false
                newServerUrl = ""
            },
            title = { Text("Add Server", color = TextPrimary) },
            text = {
                BrutalistTextField(
                    value = newServerUrl,
                    onValueChange = { newServerUrl = it },
                    label = "Server URL (https://...)",
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                BrutalistButton(
                    text = "ADD",
                    onClick = {
                        viewModel.addServer(newServerUrl)
                        showAddServerDialog = false
                        newServerUrl = ""
                    },
                    enabled = newServerUrl.startsWith("http://") || newServerUrl.startsWith("https://")
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddServerDialog = false
                    newServerUrl = ""
                }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = SecondaryBg,
            shape = RectangleShape
        )
    }
}
