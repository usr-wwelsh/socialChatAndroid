package com.socialchat.app

import android.app.Application
import android.net.Uri
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.socialchat.app.core.util.Base64Fetcher
import com.socialchat.app.core.util.Base64Keyer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SocialChatApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(Base64Fetcher.Factory(), Uri::class.java)
                add(Base64Keyer())
            }
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }
}
