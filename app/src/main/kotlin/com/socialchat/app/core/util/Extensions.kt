package com.socialchat.app.core.util

import android.util.Base64
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap

fun Bitmap.toBase64(format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 85): String {
    val out = ByteArrayOutputStream()
    compress(format, quality, out)
    val bytes = out.toByteArray()
    val mimeType = if (format == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
    return "data:$mimeType;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
}
