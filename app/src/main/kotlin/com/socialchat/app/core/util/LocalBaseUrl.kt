package com.socialchat.app.core.util

import androidx.compose.runtime.compositionLocalOf
import com.socialchat.app.core.preferences.UserPreferences

val LocalBaseUrl = compositionLocalOf { UserPreferences.DEFAULT_BASE_URL }
