package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.data.StatsState
import kotlinx.coroutines.flow.StateFlow

interface AnalyticsRepository {
    val stats: StateFlow<StatsState>

    suspend fun refreshAnalytics(range: String): AppResult<Unit>
}
