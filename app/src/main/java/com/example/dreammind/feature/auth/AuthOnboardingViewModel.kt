package com.example.dreammind.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.data.repository.AuthRepository
import com.example.dreammind.core.data.repository.OnboardingRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthOnboardingViewModel(
    private val authRepository: AuthRepository,
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthOnboardingUiState())

    val uiState: StateFlow<AuthOnboardingUiState> = _uiState.asStateFlow()

    fun updateSignInEmail(value: String) {
        _uiState.update { it.copy(signInEmail = value, errorMessage = null) }
    }

    fun updateSignInPassword(value: String) {
        _uiState.update { it.copy(signInPassword = value, errorMessage = null) }
    }

    fun updateCreateEmail(value: String) {
        _uiState.update { it.copy(createEmail = value, errorMessage = null) }
    }

    fun updateCreatePassword(value: String) {
        _uiState.update { it.copy(createPassword = value, errorMessage = null) }
    }

    fun selectGoal(goalName: String) {
        _uiState.update { it.copy(selectedGoalName = goalName) }
    }

    fun cycleBedtime(optionCount: Int) {
        _uiState.update { state ->
            state.copy(bedtimeIndex = (state.bedtimeIndex + 1) % optionCount)
        }
    }

    fun cycleWakeTime(optionCount: Int) {
        _uiState.update { state ->
            state.copy(wakeTimeIndex = (state.wakeTimeIndex + 1) % optionCount)
        }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!validateCredentials(state.createEmail, state.createPassword)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(
                email = state.createEmail,
                password = state.createPassword,
                displayName = state.createEmail.substringBefore("@").ifBlank { "DreamMind User" }
            )
            handleAuthResult(result, onSuccess)
        }
    }

    fun login(onSuccess: (Boolean) -> Unit) {
        val state = _uiState.value
        if (!validateCredentials(state.signInEmail, state.signInPassword)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(
                email = state.signInEmail,
                password = state.signInPassword
            )
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    onSuccess(result.data.user.onboardingCompleted)
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun completeOnboarding(
        bedtime: String,
        wakeTime: String,
        syncEnabled: Boolean,
        onSuccess: () -> Unit
    ) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = onboardingRepository.completeOnboarding(
                    goal = state.selectedGoalName.toBackendGoal(),
                    targetBedtime = bedtime.toTwentyFourHour(pm = true),
                    targetWakeTime = wakeTime.toTwentyFourHour(pm = false),
                    syncEnabled = syncEnabled
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    onSuccess()
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun showUnavailable(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun validateCredentials(email: String, password: String): Boolean {
        val errorMessage = when {
            email.isBlank() -> "Email is required."
            !email.contains("@") -> "Enter a valid email address."
            password.length < 6 -> "Password must be at least 6 characters."
            else -> null
        }

        if (errorMessage != null) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            return false
        }

        return true
    }

    private fun handleAuthResult(
        result: AppResult<*>,
        onSuccess: () -> Unit
    ) {
        when (result) {
            is AppResult.Success -> {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                onSuccess()
            }

            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

private fun String.toBackendGoal(): String {
    return when (this) {
        "WakeRefreshed" -> "WAKE_UP_REFRESHED"
        "TrackHealth" -> "TRACK_SLEEP_HEALTH"
        else -> "FALL_ASLEEP_FASTER"
    }
}

private fun String.toTwentyFourHour(pm: Boolean): String {
    val parts = split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return this
    val minute = parts.getOrNull(1) ?: "00"
    val normalizedHour = when {
        pm && hour < 12 -> hour + 12
        !pm && hour == 12 -> 0
        else -> hour
    }
    return "%02d:%s".format(normalizedHour, minute)
}
