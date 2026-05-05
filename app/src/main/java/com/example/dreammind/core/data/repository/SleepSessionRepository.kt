package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.data.DashboardState
import com.example.dreammind.data.SessionDetailState
import com.example.dreammind.data.SleepLogState
import kotlinx.coroutines.flow.StateFlow

interface SleepSessionRepository {
    val hasLatestSession: StateFlow<Boolean>
    val latestSessionId: StateFlow<String?>
    val dashboard: StateFlow<DashboardState>
    val sessionDetail: StateFlow<SessionDetailState>

    suspend fun refreshLatestSession(): AppResult<Unit>
    suspend fun createSleepSession(input: SleepLogState): AppResult<String>
    suspend fun loadSessionDetail(sessionId: String): AppResult<Unit>
}
