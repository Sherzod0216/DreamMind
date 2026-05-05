package com.example.dreammind.feature.sleep_log

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreammind.core.data.repository.DreamMindRepository
import com.example.dreammind.core.data.repository.SleepSessionRepository
import com.example.dreammind.core.viewmodel.dreamMindViewModelFactory

@Composable
fun SleepLogRoute(
    repository: DreamMindRepository,
    sleepSessionRepository: SleepSessionRepository,
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: SleepLogViewModel = viewModel(
        factory = dreamMindViewModelFactory {
            SleepLogViewModel(repository, sleepSessionRepository)
        }
    )
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    SleepLogScreen(
        state = uiState.sleepLog,
        isSaving = uiState.isSaving,
        errorMessage = uiState.errorMessage,
        onBack = onBack,
        onCalendarClick = viewModel::cycleSessionDate,
        onBedtimeClick = viewModel::cycleBedtime,
        onWakeTimeClick = viewModel::cycleWakeTime,
        onQualityClick = viewModel::cycleQuality,
        onActivityClick = viewModel::toggleActivity,
        onAddTag = viewModel::addCustomTag,
        onNotesChange = viewModel::updateNotes,
        onSave = {
            viewModel.saveSleepLog(onSaved)
        }
    )
}
