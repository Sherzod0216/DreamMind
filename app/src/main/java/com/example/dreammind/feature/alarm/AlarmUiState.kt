package com.example.dreammind.feature.alarm

import com.example.dreammind.data.AlarmState

data class AlarmUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val alarm: AlarmState
)
