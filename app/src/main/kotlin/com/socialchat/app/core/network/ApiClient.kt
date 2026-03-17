package com.socialchat.app.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitProvider @Inject constructor(
    private val cookieJar: SessionCookieJar
) {
    @Volatile private var retrofit: Retrofit? = null
    @Volatile private var currentBaseUrl: String = ""

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    fun getRetrofit(baseUrl: String): Retrofit {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (retrofit == null || currentBaseUrl != normalizedUrl) {
            synchronized(this) {
                if (retrofit == null || currentBaseUrl != normalizedUrl) {
                    val client = OkHttpClient.Builder()
                        .cookieJar(cookieJar)
                        .addInterceptor(loggingInterceptor)
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(20, TimeUnit.SECONDS)
                        .build()

                    retrofit = Retrofit.Builder()
                        .baseUrl(normalizedUrl)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    currentBaseUrl = normalizedUrl
                }
            }
        }
        return retrofit!!
    }

    fun rebuild(baseUrl: String) {
        synchronized(this) {
            retrofit = null
        }
        getRetrofit(baseUrl)
    }
}
