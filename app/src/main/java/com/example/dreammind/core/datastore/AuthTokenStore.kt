package com.example.dreammind.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_tokens")

data class AuthTokens(
    val accessToken: String?,
    val refreshToken: String?
)

class AuthTokenStore(
    private val context: Context
) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    val tokens: Flow<AuthTokens> = context.authDataStore.data.map { preferences ->
        AuthTokens(
            accessToken = preferences[accessTokenKey],
            refreshToken = preferences[refreshTokenKey]
        )
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
        }
    }

    suspend fun accessToken(): String? = tokens.first().accessToken

    suspend fun refreshToken(): String? = tokens.first().refreshToken
}
