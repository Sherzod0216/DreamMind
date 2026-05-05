package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.model.AuthSession
import com.example.dreammind.core.model.AuthStartupState

interface AuthRepository {
    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): AppResult<AuthSession>

    suspend fun login(
        email: String,
        password: String
    ): AppResult<AuthSession>

    suspend fun checkStartupSession(): AuthStartupState

    suspend fun logout()
}
