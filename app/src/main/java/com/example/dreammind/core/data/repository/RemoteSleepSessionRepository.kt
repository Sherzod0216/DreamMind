package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.network.DreamMindApi
import com.example.dreammind.core.network.dto.CreateSleepSessionRequest
import com.example.dreammind.core.network.dto.SleepSessionSummaryResponse
import com.example.dreammind.core.network.dto.toLatestSleepSessionOrNull
import com.example.dreammind.core.network.dto.toUiState
import com.example.dreammind.data.DashboardState
import com.example.dreammind.data.SessionDetailState
import com.example.dreammind.data.SleepLogState
import com.google.gson.Gson
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RemoteSleepSessionRepository(
    private val api: DreamMindApi,
    private val fallbackRepository: DreamMindRepository
) : SleepSessionRepository {
    private val gson = Gson()
    private val _hasLatestSession = MutableStateFlow(false)
    private val _latestSessionId = MutableStateFlow<String?>(null)
    private val _dashboard = MutableStateFlow(fallbackRepository.dashboard)
    private val _sessionDetail = MutableStateFlow(fallbackRepository.sessionDetail)

    override val hasLatestSession: StateFlow<Boolean> = _hasLatestSession
    override val latestSessionId: StateFlow<String?> = _latestSessionId
    override val dashboard: StateFlow<DashboardState> = _dashboard
    override val sessionDetail: StateFlow<SessionDetailState> = _sessionDetail

    override suspend fun refreshLatestSession(): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            api.latestSleepSession().toLatestSleepSessionOrNull(gson)
        }.fold(
            onSuccess = { latest ->
                if (latest == null) {
                    _hasLatestSession.value = false
                    _latestSessionId.value = null
                } else {
                    applyLatestSession(latest)
                }
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                AppResult.Error(error.sleepMessage("Could not load latest sleep session."), error)
            }
        )
    }

    override suspend fun createSleepSession(input: SleepLogState): AppResult<String> = withContext(Dispatchers.IO) {
        runCatching {
            api.createSleepSession(input.toRequest())
        }.fold(
            onSuccess = { created ->
                applyLatestSession(created)
                AppResult.Success(created.id)
            },
            onFailure = { error ->
                AppResult.Error(error.sleepMessage("Could not save sleep session."), error)
            }
        )
    }

    override suspend fun loadSessionDetail(sessionId: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (sessionId == "latest" && _latestSessionId.value.isNullOrBlank()) {
            refreshLatestSession()
        }
        val id = if (sessionId == "latest") _latestSessionId.value else sessionId
        if (id.isNullOrBlank()) {
            return@withContext AppResult.Error("No sleep session is available yet.")
        }

        runCatching {
            api.sleepSessionDetail(id)
        }.fold(
            onSuccess = { detail ->
                _sessionDetail.value = detail.toUiState(fallbackRepository.sessionDetail)
                AppResult.Success(Unit)
            },
            onFailure = { error ->
                AppResult.Error(error.sleepMessage("Could not load session detail."), error)
            }
        )
    }

    private fun applyLatestSession(session: SleepSessionSummaryResponse) {
        _hasLatestSession.value = true
        _latestSessionId.value = session.id
        _dashboard.value = fallbackRepository.dashboard.copy(
            lastSleepDuration = session.durationLabel,
            sleepScore = session.qualityScore,
            insightTitle = "Latest Sleep",
            insightBody = "Your latest night was ${session.durationLabel} with ${session.qualityLabel.lowercase()} quality."
        )
    }
}

private fun SleepLogState.toRequest(): CreateSleepSessionRequest {
    val wakeDate = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -sessionDateOffsetDays)
    }
    val bedtimeDate = wakeDate.clone() as Calendar
    bedtimeDate.add(Calendar.DAY_OF_YEAR, -1)

    return CreateSleepSessionRequest(
        sessionDate = wakeDate.formatDate(),
        bedtime = bedtimeDate.toIsoDateTime(bedtime),
        wakeTime = wakeDate.toIsoDateTime(wakeTime),
        qualityScore = (qualityLevel * 100).toInt().coerceIn(0, 100),
        qualityLabel = qualityLabel,
        activities = selectedActivities.map { it.toBackendActivity() },
        notes = notes.takeIf { it.isNotBlank() }
    )
}

private fun Calendar.formatDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(time)
}

private fun Calendar.toIsoDateTime(timeLabel: String): String {
    val parsedTime = SimpleDateFormat("hh:mm a", Locale.US).parse(timeLabel)
    val timeCalendar = Calendar.getInstance().apply {
        if (parsedTime != null) {
            time = parsedTime
        }
    }

    val date = clone() as Calendar
    date.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
    date.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
    date.set(Calendar.SECOND, 0)
    date.set(Calendar.MILLISECOND, 0)

    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(date.time)
}

private fun String.toBackendActivity(): String {
    return when (this) {
        "Caffeine" -> "CAFFEINE"
        "Reading" -> "READING"
        "Screen Time" -> "SCREEN_TIME"
        "Exercise" -> "EXERCISE"
        "Meditation" -> "MEDITATION"
        else -> "OTHER"
    }
}

private fun Throwable.sleepMessage(fallback: String): String {
    return when (this) {
        is HttpException -> when (code()) {
            401 -> "Your session expired. Sign in again."
            404 -> "Sleep session was not found."
            in 500..599 -> "Backend server error. Try again after the API is running."
            else -> "$fallback (${code()})"
        }

        is IOException -> "Cannot reach the DreamMind backend. Start the NestJS server or update DREAMMIND_BASE_URL."
        else -> fallback
    }
}
