package com.example.dreammind.feature.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.data.repository.CoachRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoachViewModel(
    private val coachRepository: CoachRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        CoachUiState(
            coach = coachRepository.coach.value
        )
    )

    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    fun refreshMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = coachRepository.refreshMessages()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        coach = coachRepository.coach.value
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

    fun updateDraft(value: String) {
        _uiState.update { it.copy(draftMessage = value, errorMessage = null) }
    }

    fun sendDraftMessage() {
        val content = _uiState.value.draftMessage.trim()
        if (content.isBlank()) return

        sendMessage(content)
    }

    private fun sendMessage(content: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, draftMessage = "", errorMessage = null) }
            when (val result = coachRepository.sendMessage(content)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isSending = false,
                        coach = coachRepository.coach.value
                    )
                }

                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = result.message,
                        draftMessage = content,
                        coach = coachRepository.coach.value
                    )
                }
            }
        }
    }

    fun useSuggestion(suggestion: String) {
        if (suggestion.equals("Analyze last night", ignoreCase = true)) {
            analyzeLastNight()
        } else {
            sendMessage(suggestion)
        }
    }

    private fun analyzeLastNight() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            when (val result = coachRepository.analyzeLastNight()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isSending = false,
                        coach = coachRepository.coach.value
                    )
                }

                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = result.message,
                        coach = coachRepository.coach.value
                    )
                }
            }
        }
    }
}
