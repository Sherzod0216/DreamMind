package com.example.dreammind.feature.profile

import com.example.dreammind.data.ProfileState

data class ProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
    val profile: ProfileState
)
