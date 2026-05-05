package com.example.dreammind.feature.session_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.data.repository.SleepSessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    private val sleepSessionRepository: SleepSessionRepository
) : ViewModel() {
    private val loadState = MutableStateFlow(SessionDetailLoadState())

    val uiState: StateFlow<SessionDetailUiState> = combine(
        sleepSessionRepository.sessionDetail,
        loadState
    ) { session, loadState ->
        SessionDetailUiState(
            isLoading = loadState.isLoading,
            errorMessage = loadState.errorMessage,
            session = session
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SessionDetailUiState(
            session = sleepSessionRepository.sessionDetail.value
        )
    )

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            loadState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = sleepSessionRepository.loadSessionDetail(sessionId)) {
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

private data class SessionDetailLoadState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
