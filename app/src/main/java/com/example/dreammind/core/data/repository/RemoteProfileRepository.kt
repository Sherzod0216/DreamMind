package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.network.DreamMindApi
import com.example.dreammind.core.network.dto.AnalyticsSummaryResponse
import com.example.dreammind.core.network.dto.toUiState
import com.example.dreammind.core.network.dto.toUpdateRequest
import com.example.dreammind.data.ProfileState
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RemoteProfileRepository(
    private val api: DreamMindApi,
    fallbackRepository: DreamMindRepository
) : ProfileRepository {
    private val fallback = fallbackRepository.profile
    private val _profile = MutableStateFlow(fallback)

    override val profile: StateFlow<ProfileState> = _profile

    override suspend fun refreshProfile(): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val profile = api.profile()
            val summary = runCatching { api.analyticsSummary("week") }.getOrNull()
            profile.toUiState(fallback, summary)
        }.fold(
            onSuccess = { profile ->
                _profile.value = profile
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                AppResult.Error(error.profileMessage("Could not load your profile."), error)
            }
        )
    }

    override suspend fun updateProfile(profile: ProfileState): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateProfile(profile.toUpdateRequest())
            val summary: AnalyticsSummaryResponse? = runCatching { api.analyticsSummary("week") }.getOrNull()
            response.toUiState(fallback, summary)
        }.fold(
            onSuccess = { updated ->
                _profile.value = updated
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                AppResult.Error(error.profileMessage("Could not update your profile."), error)
            }
        )
    }
}

private fun Throwable.profileMessage(fallback: String): String {
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
