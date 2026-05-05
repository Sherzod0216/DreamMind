package com.example.dreammind.feature.home

import com.example.dreammind.data.DashboardState

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSleepData: Boolean = false,
    val latestSessionId: String? = null,
    val dashboard: DashboardState
)
