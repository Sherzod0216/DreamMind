package com.example.dreammind.core.network.dto

import com.example.dreammind.data.AlarmSound
import com.example.dreammind.data.AlarmState
import com.example.dreammind.data.SleepBar
import com.example.dreammind.data.StatHighlight
import com.example.dreammind.data.StatsState

data class AlarmResponse(
    val alarmTime: String,
    val smartWakeEnabled: Boolean,
    val smartWakeWindowMinutes: Int,
    val sound: String,
    val vibrationLevel: Float
)

data class UpdateAlarmRequest(
    val alarmTime: String,
    val smartWakeEnabled: Boolean,
    val smartWakeWindowMinutes: Int,
    val sound: String,
    val vibrationLevel: Float
)

data class AnalyticsSummaryResponse(
    val range: String,
    val averageSleepMinutes: Int,
    val averageSleepLabel: String,
    val qualityPercent: Int,
    val sleepDebtMinutes: Int,
    val sleepDebtLabel: String,
    val consistencyPercent: Int,
    val averageBedtime: String,
    val averageWakeTime: String
)

data class SleepHoursResponse(
    val items: List<SleepHoursPointResponse>
)

data class SleepHoursPointResponse(
    val label: String,
    val durationMinutes: Int,
    val progress: Float
)

data class QualityResponse(
    val qualityPercent: Int,
    val trendText: String
)

fun AlarmResponse.toUiState(fallback: AlarmState): AlarmState {
    return AlarmState(
        alarmTime = alarmTime,
        alarmWindow = "Alarm set for ${alarmTime.toDisplayTime()} • ${smartWakeWindowMinutes}-minute smart wake",
        smartWakeEnabled = smartWakeEnabled,
        smartWakeWindowMinutes = smartWakeWindowMinutes,
        soundOptions = fallback.soundOptions.map { option ->
            option.copy(selected = option.title == sound)
        }.ensureSelected(sound),
        vibrationLevel = vibrationLevel
    )
}

fun AlarmState.toUpdateRequest(): UpdateAlarmRequest {
    return UpdateAlarmRequest(
        alarmTime = alarmTime,
        smartWakeEnabled = smartWakeEnabled,
        smartWakeWindowMinutes = smartWakeWindowMinutes,
        sound = soundOptions.firstOrNull { it.selected }?.title ?: "Forest Morning",
        vibrationLevel = vibrationLevel
    )
}

fun analyticsToUiState(
    summary: AnalyticsSummaryResponse,
    sleepHours: SleepHoursResponse,
    quality: QualityResponse
): StatsState {
    return StatsState(
        averageSleep = summary.averageSleepLabel,
        qualityPercent = quality.qualityPercent,
        qualityTrend = quality.trendText,
        bars = sleepHours.items.map {
            SleepBar(
                label = it.label,
                progress = it.progress
            )
        },
        highlights = listOf(
            StatHighlight(label = "Avg Bedtime", value = summary.averageBedtime),
            StatHighlight(label = "Avg Wake Time", value = summary.averageWakeTime),
            StatHighlight(label = "Sleep Debt", value = summary.sleepDebtLabel),
            StatHighlight(label = "Consistency", value = "${summary.consistencyPercent}%")
        )
    )
}

private fun List<AlarmSound>.ensureSelected(sound: String): List<AlarmSound> {
    if (any { it.selected }) return this

    return listOf(
        AlarmSound(title = sound, subtitle = "Saved alarm sound", selected = true)
    ) + this
}

private fun String.toDisplayTime(): String {
    val parts = split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return this
    val minute = parts.getOrNull(1) ?: "00"
    val meridiem = if (hour >= 12) "PM" else "AM"
    val displayHour = when (val normalized = hour % 12) {
        0 -> 12
        else -> normalized
    }
    return "$displayHour:$minute $meridiem"
}
