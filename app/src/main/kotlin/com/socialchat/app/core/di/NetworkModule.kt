package com.socialchat.app.core.di

import com.socialchat.app.core.network.RetrofitProvider
import com.socialchat.app.core.network.SessionCookieJar
import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.core.crypto.CryptoManager
import com.socialchat.app.data.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofitProvider(cookieJar: SessionCookieJar): RetrofitProvider =
        RetrofitProvider(cookieJar)

    @Provides
    @Singleton
    fun providePostApiService(provider: RetrofitProvider, prefs: UserPreferences): PostApiService {
        val url = runBlocking { prefs.baseUrl.first() }
        return provider.getRetrofit(url).create(PostApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApiService(provider: RetrofitProvider, prefs: UserPreferences): ProfileApiService {
        val url = runBlocking { prefs.baseUrl.first() }
        return provider.getRetrofit(url).create(ProfileApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFriendApiService(provider: RetrofitProvider, prefs: UserPreferences): FriendApiService {
        val url = runBlocking { prefs.baseUrl.first() }
        return provider.getRetrofit(url).create(FriendApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatroomApiService(provider: RetrofitProvider, prefs: UserPreferences): ChatroomApiService {
        val url = runBlocking { prefs.baseUrl.first() }
        return provider.getRetrofit(url).create(ChatroomApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTagApiService(provider: RetrofitProvider, prefs: UserPreferences): TagApiService {
        val url = runBlocking { prefs.baseUrl.first() }
        return provider.getRetrofit(url).create(TagApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideKeysApiService(provider: RetrofitProvider, prefs: UserPreferences): KeysApiService {
        val url = runBlocking { prefs.baseUrl.first() }
        return provider.getRetrofit(url).create(KeysApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDmApiService(provider: RetrofitProvider, prefs: UserPreferences): DmApiService {
        val url = runBlocking { prefs.baseUrl.first() }
        return provider.getRetrofit(url).create(DmApiService::class.java)
    }
}
