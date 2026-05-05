package com.example.dreammind.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.data.repository.AuthRepository
import com.example.dreammind.core.data.repository.ProfileRepository
import com.example.dreammind.data.ProfileSettingType
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ProfileUiState(
            profile = profileRepository.profile.value
        )
    )

    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, noticeMessage = null) }
            when (val result = profileRepository.refreshProfile()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = profileRepository.profile.value
                    )
                }

                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun toggleBedtimeReminder() {
        _uiState.update { state ->
            state.copy(
                noticeMessage = "Bedtime reminder updated locally for this demo.",
                profile = state.profile.copy(
                    settings = state.profile.settings.map { setting ->
                        if (setting.type == ProfileSettingType.ToggleOn) {
                            setting.copy(enabled = !setting.enabled)
                        } else {
                            setting
                        }
                    }
                )
            )
        }
    }

    fun showSettingNotice(title: String) {
        _uiState.update {
            it.copy(noticeMessage = "$title is prepared as a demo setting. Full editing can be added after the presentation.")
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
