package com.socialchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.socialchat.app.ui.theme.PrimaryBg
import com.socialchat.app.ui.theme.SocialChatTheme
import com.socialchat.app.ui.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SocialChatTheme {
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
