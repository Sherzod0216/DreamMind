package com.example.dreammind.core.data.repository

import com.example.dreammind.core.common.AppResult
import com.example.dreammind.data.ProfileState
import kotlinx.coroutines.flow.StateFlow

interface ProfileRepository {
    val profile: StateFlow<ProfileState>

    suspend fun refreshProfile(): AppResult<Unit>
    suspend fun updateProfile(profile: ProfileState): AppResult<Unit>
}
