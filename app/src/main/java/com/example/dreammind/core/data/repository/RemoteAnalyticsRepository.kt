package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.network.DreamMindApi
import com.example.dreammind.core.network.dto.analyticsToUiState
import com.example.dreammind.data.StatsState
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RemoteAnalyticsRepository(
    private val api: DreamMindApi,
    fallbackRepository: DreamMindRepository
) : AnalyticsRepository {
    private val _stats = MutableStateFlow(fallbackRepository.stats)

    override val stats: StateFlow<StatsState> = _stats

    override suspend fun refreshAnalytics(range: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        val backendRange = range.lowercase()

        runCatching {
            val summary = api.analyticsSummary(backendRange)
            val sleepHours = api.sleepHours(backendRange)
            val quality = api.quality(backendRange)

            analyticsToUiState(
                summary = summary,
                sleepHours = sleepHours,
                quality = quality
            )
        }.fold(
            onSuccess = { stats ->
                _stats.value = stats
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                AppResult.Error(error.analyticsMessage("Could not load analytics."), error)
            }
        )
    }
}

private fun Throwable.analyticsMessage(fallback: String): String {
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
