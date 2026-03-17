package com.socialchat.app.core.util

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Base64
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.key.Keyer
import coil.request.Options

class Base64Fetcher(private val data: String, private val options: Options) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val commaIdx = data.indexOf(',')
        val base64Str = if (commaIdx >= 0) data.substring(commaIdx + 1) else data
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Failed to decode base64 image")
        return DrawableResult(
            drawable = BitmapDrawable(options.context.resources, bitmap),
            isSampled = false,
            dataSource = DataSource.MEMORY
        )
    }

    class Factory : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.scheme != "data") return null
            val ssp = data.schemeSpecificPart ?: return null
            if (!ssp.startsWith("image/")) return null
            return Base64Fetcher(data.toString(), options)
        }
    }
}

class Base64Keyer : Keyer<Uri> {
    override fun key(data: Uri, options: Options): String? {
        if (data.scheme != "data") return null
        val str = data.toString()
        return "base64:${str.take(64).hashCode()}:${str.length}"
    }
}
