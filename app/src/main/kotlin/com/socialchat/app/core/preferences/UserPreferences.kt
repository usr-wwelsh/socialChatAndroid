package com.socialchat.app.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "socialchat_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val BASE_URL_KEY = stringPreferencesKey("base_url")
        private val SESSION_COOKIE_KEY = stringPreferencesKey("session_cookie")
        private val CACHED_USERNAME_KEY = stringPreferencesKey("cached_username")
        private val SERVER_LIST_KEY = stringPreferencesKey("server_list")
        val DEFAULT_BASE_URL = "https://chat.wwel.sh"
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[BASE_URL_KEY] ?: DEFAULT_BASE_URL
    }

    val serverList: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[SERVER_LIST_KEY] ?: ""
        val extras = if (json.isNotEmpty()) {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }.filter { it != DEFAULT_BASE_URL }
        } else emptyList()
        listOf(DEFAULT_BASE_URL) + extras
    }

    val sessionCookie: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SESSION_COOKIE_KEY]
    }

    val cachedUsername: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[CACHED_USERNAME_KEY]
    }

    suspend fun getBaseUrl(): String =
        context.dataStore.data.first()[BASE_URL_KEY] ?: DEFAULT_BASE_URL

    suspend fun getRawCookieHeader(): String =
        context.dataStore.data.first()[SESSION_COOKIE_KEY] ?: ""

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { it[BASE_URL_KEY] = url.trimEnd('/') }
    }

    suspend fun saveSessionCookie(cookie: String) {
        context.dataStore.edit { it[SESSION_COOKIE_KEY] = cookie }
    }

    suspend fun saveCachedUsername(username: String) {
        context.dataStore.edit { it[CACHED_USERNAME_KEY] = username }
    }

    suspend fun addServer(url: String) {
        if (url == DEFAULT_BASE_URL) return
        context.dataStore.edit { prefs ->
            val json = prefs[SERVER_LIST_KEY] ?: ""
            val list = if (json.isNotEmpty()) {
                val arr = org.json.JSONArray(json)
                (0 until arr.length()).map { arr.getString(it) }.toMutableList()
            } else mutableListOf()
            if (!list.contains(url)) {
                list.add(url)
                prefs[SERVER_LIST_KEY] = org.json.JSONArray(list).toString()
            }
        }
    }

    suspend fun removeServer(url: String) {
        if (url == DEFAULT_BASE_URL) return
        context.dataStore.edit { prefs ->
            val json = prefs[SERVER_LIST_KEY] ?: ""
            if (json.isNotEmpty()) {
                val arr = org.json.JSONArray(json)
                val list = (0 until arr.length()).map { arr.getString(it) }.filter { it != url }
                prefs[SERVER_LIST_KEY] = org.json.JSONArray(list).toString()
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(SESSION_COOKIE_KEY)
            it.remove(CACHED_USERNAME_KEY)
        }
    }

    suspend fun getCachedUsername(): String? =
        context.dataStore.data.first()[CACHED_USERNAME_KEY]

    suspend fun hasSession(): Boolean =
        !context.dataStore.data.first()[SESSION_COOKIE_KEY].isNullOrEmpty()
}
