package com.socialchat.app.core.network

import com.socialchat.app.core.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCookieJar @Inject constructor(
    private val prefs: UserPreferences
) : CookieJar {

    private val inMemoryCookies = mutableListOf<Cookie>()
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Hydrate from DataStore on startup
        runBlocking {
            val raw = prefs.getRawCookieHeader()
            if (raw.isNotEmpty()) {
                val baseUrl = prefs.getBaseUrl()
                val httpUrl = baseUrl.toHttpUrlOrNull() ?: return@runBlocking
                val headers = Headers.Builder().add("Set-Cookie", raw).build()
                val cookies = Cookie.parseAll(httpUrl, headers)
                inMemoryCookies.addAll(cookies)
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val sessionCookie = cookies.find { it.name == "connect.sid" } ?: return
        synchronized(inMemoryCookies) {
            inMemoryCookies.removeAll { it.name == "connect.sid" }
            inMemoryCookies.add(sessionCookie)
        }
        // Persist asynchronously
        scope.launch {
            prefs.saveSessionCookie("connect.sid=${sessionCookie.value}")
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return synchronized(inMemoryCookies) {
            inMemoryCookies.toList()
        }
    }

    fun clearCookies() {
        synchronized(inMemoryCookies) {
            inMemoryCookies.clear()
        }
        scope.launch { prefs.clearSession() }
    }
}
