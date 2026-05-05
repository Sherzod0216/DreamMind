package com.example.dreammind.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.data.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AnalyticsUiState(
            stats = analyticsRepository.stats.value
        )
    )

    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun selectPeriod(period: String) {
        _uiState.update { state ->
            state.copy(selectedPeriod = period)
        }
        refreshAnalytics()
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = analyticsRepository.refreshAnalytics(_uiState.value.selectedPeriod)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        stats = analyticsRepository.stats.value
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
}
