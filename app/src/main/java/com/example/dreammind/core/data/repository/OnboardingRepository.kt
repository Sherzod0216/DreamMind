package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult

interface OnboardingRepository {
    suspend fun completeOnboarding(
        goal: String,
        targetBedtime: String,
        targetWakeTime: String,
        syncEnabled: Boolean
    ): AppResult<Unit>
}
