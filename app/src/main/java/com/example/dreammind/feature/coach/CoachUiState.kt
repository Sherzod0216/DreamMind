package com.example.dreammind.feature.coach

import com.example.dreammind.data.CoachState

data class CoachUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val draftMessage: String = "",
    val coach: CoachState
)
