package com.example.dreammind.feature.sleep_log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.data.repository.DreamMindRepository
import com.example.dreammind.core.data.repository.SleepSessionRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SleepLogViewModel(
    repository: DreamMindRepository,
    private val sleepSessionRepository: SleepSessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SleepLogUiState(
            sleepLog = repository.sleepLog
        )
    )

    val uiState: StateFlow<SleepLogUiState> = _uiState.asStateFlow()

    fun cycleSessionDate() {
        _uiState.update { state ->
            state.copy(
                sleepLog = state.sleepLog.copy(
                    sessionDateOffsetDays = (state.sleepLog.sessionDateOffsetDays + 1) % 3
                )
            )
        }
    }

    fun cycleBedtime() {
        _uiState.update { state ->
            state.copy(
                sleepLog = state.sleepLog.copy(
                    bedtime = state.sleepLog.bedtime.nextFrom(BEDTIME_OPTIONS)
                )
            )
        }
    }

    fun cycleWakeTime() {
        _uiState.update { state ->
            state.copy(
                sleepLog = state.sleepLog.copy(
                    wakeTime = state.sleepLog.wakeTime.nextFrom(WAKE_OPTIONS)
                )
            )
        }
    }

    fun cycleQuality() {
        _uiState.update { state ->
            val nextLevel = when {
                state.sleepLog.qualityLevel < 0.45f -> 0.65f
                state.sleepLog.qualityLevel < 0.75f -> 0.85f
                state.sleepLog.qualityLevel < 0.95f -> 1.0f
                else -> 0.3f
            }
            state.copy(
                sleepLog = state.sleepLog.copy(
                    qualityLevel = nextLevel,
                    qualityLabel = nextLevel.toQualityLabel()
                )
            )
        }
    }

    fun toggleActivity(activity: String) {
        _uiState.update { state ->
            val selected = state.sleepLog.selectedActivities
            state.copy(
                sleepLog = state.sleepLog.copy(
                    selectedActivities = if (selected.contains(activity)) {
                        selected - activity
                    } else {
                        selected + activity
                    }
                )
            )
        }
    }

    fun addCustomTag() {
        _uiState.update { state ->
            val nextTag = CUSTOM_TAGS.firstOrNull { it !in state.sleepLog.allActivities } ?: "Relaxed"
            val allActivities = if (nextTag in state.sleepLog.allActivities) {
                state.sleepLog.allActivities
            } else {
                state.sleepLog.allActivities + nextTag
            }
            state.copy(
                sleepLog = state.sleepLog.copy(
                    allActivities = allActivities,
                    selectedActivities = state.sleepLog.selectedActivities + nextTag
                )
            )
        }
    }

    fun updateNotes(value: String) {
        _uiState.update { state ->
            state.copy(
                sleepLog = state.sleepLog.copy(notes = value)
            )
        }
    }

    fun saveSleepLog(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            when (val result = sleepSessionRepository.createSleepSession(_uiState.value.sleepLog)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    onSaved(result.data)
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}

private val BEDTIME_OPTIONS = listOf("10:00 PM", "10:30 PM", "10:45 PM", "11:00 PM", "11:30 PM", "12:00 AM")
private val WAKE_OPTIONS = listOf("06:15 AM", "06:45 AM", "07:00 AM", "07:30 AM", "08:00 AM")
private val CUSTOM_TAGS = listOf("Relaxed", "Warm Tea", "No Screens")

private fun String.nextFrom(options: List<String>): String {
    val index = options.indexOf(this).takeIf { it >= 0 } ?: 0
    return options[(index + 1) % options.size]
}

private fun Float.toQualityLabel(): String {
    return when {
        this < 0.45f -> "Restless"
        this < 0.75f -> "Fair"
        this < 0.95f -> "Good"
        else -> "Peaceful"
    }
}
