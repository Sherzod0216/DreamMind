package com.example.dreammind.feature.session_detail

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreammind.core.data.repository.SleepSessionRepository
import com.example.dreammind.core.viewmodel.dreamMindViewModelFactory

@Composable
fun SessionDetailRoute(
    sleepSessionRepository: SleepSessionRepository,
    sessionId: String,
    onBack: () -> Unit,
    viewModel: SessionDetailViewModel = viewModel(
        factory = dreamMindViewModelFactory {
            SessionDetailViewModel(sleepSessionRepository)
        }
    )
) {
    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    SleepSessionDetailScreen(
        state = uiState.session,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onBack = onBack
    )
}
