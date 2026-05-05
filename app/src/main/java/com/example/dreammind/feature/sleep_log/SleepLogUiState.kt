package com.example.dreammind.feature.sleep_log

import com.example.dreammind.data.SleepLogState

data class SleepLogUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val sleepLog: SleepLogState
)
