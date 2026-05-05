package com.example.dreammind.core.network

import com.example.dreammind.core.datastore.AuthTokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: AuthTokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking { tokenStore.accessToken() }
        val request = if (accessToken.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }

        return chain.proceed(request)
    }
}
