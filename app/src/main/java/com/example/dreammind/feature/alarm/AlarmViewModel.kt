package com.example.dreammind.feature.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.alarm.AlarmScheduler
import com.example.dreammind.core.data.repository.AlarmRepository
import com.example.dreammind.data.AlarmSound
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AlarmUiState(
            alarm = alarmRepository.alarm.value
        )
    )

    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    fun refreshAlarm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = alarmRepository.refreshAlarm()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        alarm = alarmRepository.alarm.value
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

    fun updateSmartWake(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                errorMessage = null,
                alarm = state.alarm.copy(smartWakeEnabled = enabled)
            )
        }
    }

    fun cycleAlarmTime() {
        _uiState.update { state ->
            val nextTime = state.alarm.alarmTime.addMinutes(15)
            state.copy(
                errorMessage = null,
                alarm = state.alarm.copy(
                    alarmTime = nextTime,
                    alarmWindow = nextTime.toAlarmWindow(state.alarm.smartWakeWindowMinutes)
                )
            )
        }
    }

    fun cycleSmartWakeWindow() {
        _uiState.update { state ->
            val options = listOf(15, 30, 45)
            val currentIndex = options.indexOf(state.alarm.smartWakeWindowMinutes).takeIf { it >= 0 } ?: 1
            val nextWindow = options[(currentIndex + 1) % options.size]
            state.copy(
                errorMessage = null,
                alarm = state.alarm.copy(
                    smartWakeWindowMinutes = nextWindow,
                    alarmWindow = state.alarm.alarmTime.toAlarmWindow(nextWindow)
                )
            )
        }
    }

    fun cycleVibration() {
        _uiState.update { state ->
            val nextLevel = when {
                state.alarm.vibrationLevel < 0.45f -> 0.65f
                state.alarm.vibrationLevel < 0.85f -> 1.0f
                else -> 0.25f
            }
            state.copy(
                errorMessage = null,
                alarm = state.alarm.copy(vibrationLevel = nextLevel)
            )
        }
    }

    fun selectSound(sound: AlarmSound) {
        _uiState.update { state ->
            state.copy(
                errorMessage = null,
                alarm = state.alarm.copy(
                    soundOptions = state.alarm.soundOptions.map {
                        it.copy(selected = it.title == sound.title)
                    }
                )
            )
        }
    }

    fun saveAlarm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = alarmRepository.updateAlarm(_uiState.value.alarm)) {
                is AppResult.Success -> _uiState.update {
                    alarmScheduler?.schedule(alarmRepository.alarm.value)
                    it.copy(
                        isSaving = false,
                        alarm = alarmRepository.alarm.value
                    )
                }

                is AppResult.Error -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}

private fun String.addMinutes(minutesToAdd: Int): String {
    val parts = split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val total = ((hour * 60 + minute + minutesToAdd) % (24 * 60))
    return "%02d:%02d".format(Locale.US, total / 60, total % 60)
}

private fun String.toAlarmWindow(windowMinutes: Int): String {
    return "Alarm set for ${toDisplayTime()} • $windowMinutes-minute smart wake"
}

private fun String.toDisplayTime(): String {
    val parts = split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return this
    val minute = parts.getOrNull(1) ?: "00"
    val meridiem = if (hour >= 12) "PM" else "AM"
    val displayHour = when (val normalized = hour % 12) {
        0 -> 12
        else -> normalized
    }
    return "$displayHour:$minute $meridiem"
}
