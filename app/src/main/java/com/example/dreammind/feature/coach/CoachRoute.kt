package com.example.dreammind.feature.coach

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreammind.core.data.repository.CoachRepository
import com.example.dreammind.core.viewmodel.dreamMindViewModelFactory

@Composable
fun CoachRoute(
    coachRepository: CoachRepository,
    viewModel: CoachViewModel = viewModel(
        factory = dreamMindViewModelFactory {
            CoachViewModel(coachRepository)
        }
    )
) {
    LaunchedEffect(Unit) {
        viewModel.refreshMessages()
    }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    CoachScreen(
        coach = uiState.coach,
        draftMessage = uiState.draftMessage,
        isSending = uiState.isSending,
        errorMessage = uiState.errorMessage,
        onDraftChange = viewModel::updateDraft,
        onSend = viewModel::sendDraftMessage,
        onSuggestionSelected = viewModel::useSuggestion
    )
}
