package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.network.DreamMindApi
import com.example.dreammind.core.network.dto.UpdateOnboardingRequest
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RemoteOnboardingRepository(
    private val api: DreamMindApi
) : OnboardingRepository {
    override suspend fun completeOnboarding(
        goal: String,
        targetBedtime: String,
        targetWakeTime: String,
        syncEnabled: Boolean
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.updateOnboarding(
                UpdateOnboardingRequest(
                    goal = goal,
                    targetBedtime = targetBedtime,
                    targetWakeTime = targetWakeTime,
                    syncEnabled = syncEnabled,
                    completed = true
                )
            )
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { error ->
                AppResult.Error(error.onboardingMessage("Could not save onboarding preferences."), error)
            }
        )
    }
}

private fun Throwable.onboardingMessage(fallback: String): String {
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
