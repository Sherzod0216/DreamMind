package com.example.dreammind.core.model

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUser
)

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String,
    val onboardingCompleted: Boolean = false
)

sealed interface AuthStartupState {
    data object Checking : AuthStartupState
    data object Unauthenticated : AuthStartupState
    data class Authenticated(val user: AuthUser) : AuthStartupState
}
