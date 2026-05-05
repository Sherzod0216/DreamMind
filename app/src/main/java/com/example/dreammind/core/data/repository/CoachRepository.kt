package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.data.CoachState
import kotlinx.coroutines.flow.StateFlow

interface CoachRepository {
    val coach: StateFlow<CoachState>

    suspend fun refreshMessages(): AppResult<Unit>
    suspend fun sendMessage(content: String): AppResult<Unit>
    suspend fun analyzeLastNight(): AppResult<Unit>
}
