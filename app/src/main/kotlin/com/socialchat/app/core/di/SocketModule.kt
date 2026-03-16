package com.socialchat.app.core.di

import com.socialchat.app.core.preferences.UserPreferences
import com.socialchat.app.core.socket.SocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {
    @Provides
    @Singleton
    fun provideSocketManager(prefs: UserPreferences): SocketManager = SocketManager(prefs)
}
