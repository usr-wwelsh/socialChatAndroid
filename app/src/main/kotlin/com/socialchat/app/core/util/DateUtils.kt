package com.socialchat.app.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun relativeTime(isoString: String?): String {
        if (isoString == null) return ""
        return try {
            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            )
            var date: Date? = null
            for (fmt in formats) {
                try { date = fmt.parse(isoString); break } catch (_: Exception) {}
            }
            val diff = System.currentTimeMillis() - (date?.time ?: return isoString)
            when {
                diff < 60_000 -> "just now"
                diff < 3_600_000 -> "${diff / 60_000}m ago"
                diff < 86_400_000 -> "${diff / 3_600_000}h ago"
                diff < 604_800_000 -> "${diff / 86_400_000}d ago"
                else -> SimpleDateFormat("MMM d", Locale.US).format(date)
            }
        } catch (_: Exception) { isoString }
    }
}
