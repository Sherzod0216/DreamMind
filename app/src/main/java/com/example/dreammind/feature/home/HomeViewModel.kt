package com.example.dreammind.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.data.repository.SleepSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val sleepSessionRepository: SleepSessionRepository
) : ViewModel() {
    private val loadState = MutableStateFlow(HomeLoadState())

    val uiState: StateFlow<HomeUiState> = combine(
        sleepSessionRepository.hasLatestSession,
        sleepSessionRepository.latestSessionId,
        sleepSessionRepository.dashboard,
        loadState
    ) { hasSleepData, latestSessionId, dashboard, loadState ->
            HomeUiState(
                isLoading = loadState.isLoading,
                errorMessage = loadState.errorMessage,
                hasSleepData = hasSleepData,
                latestSessionId = latestSessionId,
                dashboard = dashboard
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(
                hasSleepData = sleepSessionRepository.hasLatestSession.value,
                latestSessionId = sleepSessionRepository.latestSessionId.value,
                dashboard = sleepSessionRepository.dashboard.value
            )
        )

    fun refreshLatestSession() {
        viewModelScope.launch {
            loadState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = sleepSessionRepository.refreshLatestSession()) {
                is AppResult.Success -> loadState.update { it.copy(isLoading = false) }
                is AppResult.Error -> loadState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

private data class HomeLoadState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
