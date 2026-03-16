package com.socialchat.app

import android.app.Application
import android.net.Uri
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.socialchat.app.core.util.Base64Fetcher
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SocialChatApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(Base64Fetcher.Factory(), Uri::class.java)
            }
            .crossfade(true)
            .build()
    }
}
