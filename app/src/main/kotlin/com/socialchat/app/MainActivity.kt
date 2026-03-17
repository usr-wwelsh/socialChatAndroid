package com.socialchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.core.util.LocalBaseUrl
import com.socialchat.app.ui.theme.PrimaryBg
import com.socialchat.app.ui.theme.SocialChatTheme
import com.socialchat.app.ui.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val baseUrl by userPreferences.baseUrl.collectAsState(initial = UserPreferences.DEFAULT_BASE_URL)
            SocialChatTheme {
                CompositionLocalProvider(LocalBaseUrl provides baseUrl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = PrimaryBg
                    ) {
                        AppNavGraph()
                    }
                }
            }
        }
    }
}
