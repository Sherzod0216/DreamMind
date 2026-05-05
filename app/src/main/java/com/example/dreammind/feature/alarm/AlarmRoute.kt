package com.example.dreammind.feature.alarm

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreammind.core.alarm.AlarmScheduler
import com.example.dreammind.core.data.repository.AlarmRepository
import com.example.dreammind.core.viewmodel.dreamMindViewModelFactory

@Composable
fun AlarmRoute(
    alarmRepository: AlarmRepository
) {
    val context = LocalContext.current.applicationContext
    val viewModel: AlarmViewModel = viewModel(
        factory = dreamMindViewModelFactory {
            AlarmViewModel(alarmRepository, AlarmScheduler(context))
        }
    )

    LaunchedEffect(Unit) {
        viewModel.refreshAlarm()
    }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val alarm = uiState.alarm

    AlarmScreen(
        time = alarm.alarmTime,
        windowLabel = alarm.alarmWindow,
        smartWakeEnabled = alarm.smartWakeEnabled,
        soundOptions = alarm.soundOptions,
        vibrationLevel = alarm.vibrationLevel,
        isSaving = uiState.isSaving,
        errorMessage = uiState.errorMessage,
        onTimeClick = viewModel::cycleAlarmTime,
        onSmartWakeChanged = viewModel::updateSmartWake,
        onSmartWakeWindowClick = viewModel::cycleSmartWakeWindow,
        onSoundSelected = viewModel::selectSound,
        onVibrationClick = viewModel::cycleVibration,
        onSave = viewModel::saveAlarm
    )
}
