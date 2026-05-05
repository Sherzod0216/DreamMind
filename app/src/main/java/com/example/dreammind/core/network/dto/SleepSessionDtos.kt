package com.example.dreammind.core.network.dto

import com.example.dreammind.data.SessionDetailState
import com.example.dreammind.data.SessionMetric
import com.example.dreammind.data.SleepStageBar
import com.example.dreammind.data.SleepStageKind
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Locale

data class CreateSleepSessionRequest(
    val sessionDate: String,
    val bedtime: String,
    val wakeTime: String,
    val qualityScore: Int,
    val qualityLabel: String,
    val activities: List<String>,
    val notes: String? = null
)

data class SleepSessionSummaryResponse(
    val id: String,
    val sessionDate: String,
    val durationMinutes: Int,
    val durationLabel: String,
    val qualityScore: Int,
    val qualityLabel: String,
    val activities: List<String>? = null
)

data class SleepSessionDetailResponse(
    val id: String,
    val sessionDate: String,
    val dateLabel: String?,
    val durationMinutes: Int,
    val durationLabel: String,
    val qualityScore: Int,
    val qualityLabel: String,
    val activities: List<String>,
    val bedtime: String? = null,
    val wakeTime: String? = null,
    val notes: String? = null,
    val stages: List<SleepStageSegmentResponse>,
    val metrics: List<SleepSessionMetricResponse>,
    val coachInsight: String?
)

data class SleepStageSegmentResponse(
    val stage: String,
    val startOffsetMinutes: Int,
    val durationMinutes: Int,
    val heightFraction: Float
)

data class SleepSessionMetricResponse(
    val label: String,
    val value: String,
    val trend: String,
    val progress: Float
)

fun JsonObject.toLatestSleepSessionOrNull(gson: Gson): SleepSessionSummaryResponse? {
    if (has("session")) {
        val session = get("session")
        return if (session == null || session.isJsonNull) {
            null
        } else {
            gson.fromJson(session, SleepSessionSummaryResponse::class.java)
        }
    }

    return gson.fromJson(this, SleepSessionSummaryResponse::class.java)
}

fun SleepSessionDetailResponse.toUiState(fallback: SessionDetailState): SessionDetailState {
    return SessionDetailState(
        dateLabel = dateLabel ?: sessionDate,
        duration = durationLabel,
        score = qualityScore,
        insight = coachInsight ?: fallback.insight,
        timelineStart = bedtime.toTimeLabel() ?: fallback.timelineStart,
        timelineMiddle = midpointLabel(bedtime, wakeTime) ?: fallback.timelineMiddle,
        timelineEnd = wakeTime.toTimeLabel() ?: fallback.timelineEnd,
        stages = if (stages.isEmpty()) fallback.stages else stages.map { it.toUiState() },
        metrics = metrics.ifEmpty {
            fallback.metrics.map {
                SleepSessionMetricResponse(
                    label = it.label,
                    value = it.value,
                    trend = it.trend,
                    progress = it.progress
                )
            }
        }.map { it.toUiState() },
        activities = activities.map { it.toReadableActivity() },
        notes = notes
    )
}

private fun SleepStageSegmentResponse.toUiState(): SleepStageBar {
    return SleepStageBar(
        kind = when (stage.uppercase()) {
            "WAKE" -> SleepStageKind.Wake
            "REM" -> SleepStageKind.Rem
            "LIGHT" -> SleepStageKind.Light
            "DEEP" -> SleepStageKind.Deep
            else -> SleepStageKind.Light
        },
        widthFraction = durationMinutes.toFloat().coerceAtLeast(1f),
        heightFraction = heightFraction.coerceIn(0.1f, 1f)
    )
}

private fun SleepSessionMetricResponse.toUiState(): SessionMetric {
    return SessionMetric(
        label = label,
        value = value,
        trend = trend,
        progress = progress
    )
}

private fun String?.toTimeLabel(): String? {
    if (isNullOrBlank()) return null
    return parseIsoDate()?.let {
        SimpleDateFormat("h:mm a", Locale.US).format(it)
    }
}

private fun midpointLabel(
    start: String?,
    end: String?
): String? {
    val startDate = start.parseIsoDate() ?: return null
    val endDate = end.parseIsoDate() ?: return null
    val midpoint = startDate.time + ((endDate.time - startDate.time) / 2)
    return SimpleDateFormat("h:mm a", Locale.US).format(midpoint)
}

private fun String?.parseIsoDate(): java.util.Date? {
    if (isNullOrBlank()) return null
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    return patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).parse(this)
        }.getOrNull()
    }
}

private fun String.toReadableActivity(): String {
    return lowercase()
        .split("_")
        .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
}
