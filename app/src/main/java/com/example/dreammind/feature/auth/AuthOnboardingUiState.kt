package com.example.dreammind.feature.auth

data class AuthOnboardingUiState(
    val signInEmail: String = "",
    val signInPassword: String = "",
    val createEmail: String = "",
    val createPassword: String = "",
    val selectedGoalName: String = "FallAsleepFaster",
    val bedtimeIndex: Int = 1,
    val wakeTimeIndex: Int = 1,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
