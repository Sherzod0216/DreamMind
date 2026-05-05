package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.network.DreamMindApi
import com.example.dreammind.core.network.dto.toUiState
import com.example.dreammind.core.network.dto.toUpdateRequest
import com.example.dreammind.data.AlarmState
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RemoteAlarmRepository(
    private val api: DreamMindApi,
    private val fallbackRepository: DreamMindRepository
) : AlarmRepository {
    private val _alarm = MutableStateFlow(fallbackRepository.alarm)

    override val alarm: StateFlow<AlarmState> = _alarm

    override suspend fun refreshAlarm(): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.getAlarm()
        }.fold(
            onSuccess = { response ->
                _alarm.value = response.toUiState(fallbackRepository.alarm)
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                AppResult.Error(error.alarmMessage("Could not load alarm settings."), error)
            }
        )
    }

    override suspend fun updateAlarm(alarm: AlarmState): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.updateAlarm(alarm.toUpdateRequest())
        }.fold(
            onSuccess = { response ->
                _alarm.value = response.toUiState(fallbackRepository.alarm)
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                AppResult.Error(error.alarmMessage("Could not save alarm settings."), error)
            }
        )
    }
}

private fun Throwable.alarmMessage(fallback: String): String {
    return when (this) {
        is HttpException -> when (code()) {
            401 -> "Your session expired. Sign in again."
            in 500..599 -> "Backend server error. Try again after the API is running."
            else -> "$fallback (${code()})"
        }

        is IOException -> "Cannot reach the DreamMind backend. Start the NestJS server or update DREAMMIND_BASE_URL."
        else -> fallback
    }
}
