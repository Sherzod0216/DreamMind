package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.datastore.AuthTokenStore
import com.example.dreammind.core.model.AuthSession
import com.example.dreammind.core.model.AuthStartupState
import com.example.dreammind.core.network.DreamMindApi
import com.example.dreammind.core.network.dto.LoginRequest
import com.example.dreammind.core.network.dto.RefreshTokenRequest
import com.example.dreammind.core.network.dto.RegisterRequest
import com.example.dreammind.core.network.dto.toDomain
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RemoteAuthRepository(
    private val api: DreamMindApi,
    private val tokenStore: AuthTokenStore
) : AuthRepository {
    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): AppResult<AuthSession> = withContext(Dispatchers.IO) {
        runCatching {
            api.register(
                RegisterRequest(
                    email = email.trim(),
                    password = password,
                    displayName = displayName.trim()
                )
            ).toDomain()
        }.fold(
            onSuccess = { session ->
                tokenStore.saveTokens(session.accessToken, session.refreshToken)
                AppResult.Success(session.withFreshUser())
            },
            onFailure = { error ->
                AppResult.Error(error.authMessage("Could not create your account."), error)
            }
        )
    }

    override suspend fun login(
        email: String,
        password: String
    ): AppResult<AuthSession> = withContext(Dispatchers.IO) {
        runCatching {
            api.login(
                LoginRequest(
                    email = email.trim(),
                    password = password
                )
            ).toDomain()
        }.fold(
            onSuccess = { session ->
                tokenStore.saveTokens(session.accessToken, session.refreshToken)
                AppResult.Success(session.withFreshUser())
            },
            onFailure = { error ->
                AppResult.Error(error.authMessage("Could not sign you in."), error)
            }
        )
    }

    override suspend fun checkStartupSession(): AuthStartupState = withContext(Dispatchers.IO) {
        val accessToken = tokenStore.accessToken()
        if (accessToken.isNullOrBlank()) {
            return@withContext AuthStartupState.Unauthenticated
        }

        runCatching {
            api.me().toDomain()
        }.fold(
            onSuccess = { user ->
                AuthStartupState.Authenticated(user)
            },
            onFailure = {
                if (refreshTokens()) {
                    runCatching { api.me().toDomain() }
                        .getOrNull()
                        ?.let { user -> AuthStartupState.Authenticated(user) }
                        ?: clearAndUnauthenticated()
                } else {
                    clearAndUnauthenticated()
                }
            }
        )
    }

    override suspend fun logout() {
        tokenStore.clear()
    }

    private suspend fun refreshTokens(): Boolean {
        val refreshToken = tokenStore.refreshToken() ?: return false
        return runCatching {
            api.refresh(RefreshTokenRequest(refreshToken))
        }.fold(
            onSuccess = { response ->
                tokenStore.saveTokens(response.accessToken, response.refreshToken)
                true
            },
            onFailure = {
                false
            }
        )
    }

    private suspend fun AuthSession.withFreshUser(): AuthSession {
        return runCatching {
            copy(user = api.me().toDomain())
        }.getOrElse { this }
    }

    private suspend fun clearAndUnauthenticated(): AuthStartupState {
        tokenStore.clear()
        return AuthStartupState.Unauthenticated
    }
}

private fun Throwable.authMessage(fallback: String): String {
    return when (this) {
        is HttpException -> when (code()) {
            400 -> "Please check the form and try again."
            401 -> "Invalid email or password."
            409 -> "An account with this email already exists."
            in 500..599 -> "Backend server error. Try again after the API is running."
            else -> "$fallback (${code()})"
        }

        is IOException -> "Cannot reach the DreamMind backend. Start the NestJS server or update DREAMMIND_BASE_URL."
        else -> fallback
    }
}
