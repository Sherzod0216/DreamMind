package com.example.dreammind.core.network

import com.example.dreammind.BuildConfig
import com.example.dreammind.core.datastore.AuthTokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DreamMindNetwork {
    fun createApi(
        tokenStore: AuthTokenStore,
        baseUrl: String = BuildConfig.DREAMMIND_BASE_URL
    ): DreamMindApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore))
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DreamMindApi::class.java)
    }
}
