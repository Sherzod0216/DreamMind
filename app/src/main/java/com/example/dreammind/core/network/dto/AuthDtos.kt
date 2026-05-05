package com.example.dreammind.core.network.dto

import com.example.dreammind.core.model.AuthSession
import com.example.dreammind.core.model.AuthUser

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUserResponse
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class AuthUserResponse(
    val id: String,
    val email: String,
    val displayName: String
)

data class MeResponse(
    val id: String,
    val email: String,
    val profile: ProfileResponse?,
    val onboardingCompleted: Boolean
)

data class ProfileResponse(
    val displayName: String?,
    val age: Int?,
    val heightCm: Int?,
    val weightKg: Int?,
    val avatarUrl: String?,
    val memberSince: String? = null
)

fun AuthResponse.toDomain(): AuthSession {
    return AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toDomain()
    )
}

fun AuthUserResponse.toDomain(): AuthUser {
    return AuthUser(
        id = id,
        email = email,
        displayName = displayName
    )
}

fun MeResponse.toDomain(): AuthUser {
    return AuthUser(
        id = id,
        email = email,
        displayName = profile?.displayName ?: email.substringBefore("@"),
        onboardingCompleted = onboardingCompleted
    )
}
