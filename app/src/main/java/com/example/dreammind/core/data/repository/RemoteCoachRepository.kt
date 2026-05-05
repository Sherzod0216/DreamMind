package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.network.DreamMindApi
import com.example.dreammind.core.network.dto.SendCoachMessageRequest
import com.example.dreammind.core.network.dto.nowCoachMessage
import com.example.dreammind.core.network.dto.toUiMessage
import com.example.dreammind.core.network.dto.toUiState
import com.example.dreammind.data.CoachState
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RemoteCoachRepository(
    private val api: DreamMindApi,
    fallbackRepository: DreamMindRepository
) : CoachRepository {
    private val fallback = fallbackRepository.coach
    private val _coach = MutableStateFlow(fallback)

    override val coach: StateFlow<CoachState> = _coach

    override suspend fun refreshMessages(): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.coachMessages()
        }.fold(
            onSuccess = { response ->
                _coach.value = response.toUiState(fallback)
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                AppResult.Error(error.coachMessage("Could not load coach messages."), error)
            }
        )
    }

    override suspend fun sendMessage(content: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank()) {
            return@withContext AppResult.Error("Message cannot be empty.")
        }

        _coach.value = _coach.value.copy(
            messages = _coach.value.messages + nowCoachMessage(trimmedContent, fromCoach = false)
        )

        runCatching {
            api.sendCoachMessage(SendCoachMessageRequest(trimmedContent))
        }.fold(
            onSuccess = { response ->
                val currentWithoutOptimistic = _coach.value.messages.dropLast(1)
                _coach.value = _coach.value.copy(
                    messages = currentWithoutOptimistic + response.userMessage.toUiMessage() + response.assistantMessage.toUiMessage()
                )
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                _coach.value = _coach.value.copy(
                    messages = _coach.value.messages.dropLast(1)
                )
                AppResult.Error(error.coachMessage("Could not send coach message."), error)
            }
        )
    }

    override suspend fun analyzeLastNight(): AppResult<Unit> = withContext(Dispatchers.IO) {
        _coach.value = _coach.value.copy(
            messages = _coach.value.messages + nowCoachMessage("Analyzing last night...", fromCoach = false)
        )

        runCatching {
            api.analyzeLastNight()
        }.fold(
            onSuccess = { response ->
                val currentWithoutOptimistic = _coach.value.messages.dropLast(1)
                _coach.value = _coach.value.copy(
                    messages = currentWithoutOptimistic + response.message.toUiMessage()
                )
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                _coach.value = _coach.value.copy(
                    messages = _coach.value.messages.dropLast(1)
                )
                AppResult.Error(error.coachMessage("Could not analyze last night."), error)
            }
        )
    }
}

private fun Throwable.coachMessage(fallback: String): String {
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
