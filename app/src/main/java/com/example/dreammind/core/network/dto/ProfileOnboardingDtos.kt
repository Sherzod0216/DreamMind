package com.example.dreammind.core.network.dto

import com.example.dreammind.data.ProfileSetting
import com.example.dreammind.data.ProfileState
import com.example.dreammind.data.ProfileStat
import java.text.SimpleDateFormat
import java.util.Locale

data class UpdateProfileRequest(
    val displayName: String,
    val age: Int?,
    val heightCm: Int?,
    val weightKg: Int?
)

data class OnboardingResponse(
    val goal: String?,
    val targetBedtime: String?,
    val targetWakeTime: String?,
    val syncEnabled: Boolean,
    val completedAt: String?
)

data class UpdateOnboardingRequest(
    val goal: String,
    val targetBedtime: String,
    val targetWakeTime: String,
    val syncEnabled: Boolean,
    val completed: Boolean
)

fun ProfileResponse.toUiState(
    fallback: ProfileState,
    summary: AnalyticsSummaryResponse?
): ProfileState {
    val personalSubtitle = listOfNotNull(
        age?.let { "$it years" },
        heightCm?.let { "$it cm" },
        weightKg?.let { "$it kg" }
    ).joinToString(" • ").ifBlank { "Height, weight, age" }

    return fallback.copy(
        userName = displayName?.takeIf { it.isNotBlank() } ?: fallback.userName,
        memberLabel = memberSince.toMemberLabel() ?: fallback.memberLabel,
        stats = summary?.toProfileStats() ?: fallback.stats,
        settings = fallback.settings.map { setting ->
            if (setting.title == "Personal Information") {
                setting.copy(subtitle = personalSubtitle)
            } else {
                setting
            }
        }
    )
}

fun ProfileState.toUpdateRequest(): UpdateProfileRequest {
    return UpdateProfileRequest(
        displayName = userName,
        age = null,
        heightCm = null,
        weightKg = null
    )
}

private fun AnalyticsSummaryResponse.toProfileStats(): List<ProfileStat> {
    return listOf(
        ProfileStat(label = "Avg Sleep", value = averageSleepLabel),
        ProfileStat(label = "Consistency", value = "$consistencyPercent%"),
        ProfileStat(label = "Sleep Debt", value = sleepDebtLabel)
    )
}

private fun String?.toMemberLabel(): String? {
    if (isNullOrBlank()) return null
    return runCatching {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val output = SimpleDateFormat("MMM yyyy", Locale.US)
        "Member since ${output.format(input.parse(this) ?: return null)}"
    }.getOrNull()
}
