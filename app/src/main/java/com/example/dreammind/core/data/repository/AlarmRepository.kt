package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.data.AlarmState
import kotlinx.coroutines.flow.StateFlow

interface AlarmRepository {
    val alarm: StateFlow<AlarmState>

    suspend fun refreshAlarm(): AppResult<Unit>
    suspend fun updateAlarm(alarm: AlarmState): AppResult<Unit>
}
