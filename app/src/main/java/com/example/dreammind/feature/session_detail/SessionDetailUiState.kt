package com.example.dreammind.feature.session_detail

import com.example.dreammind.data.SessionDetailState

data class SessionDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val session: SessionDetailState
)
