package com.example.dreammind.feature.analytics

import com.example.dreammind.data.StatsState

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedPeriod: String = "Week",
    val stats: StatsState
)
