package com.example.dreammind.feature.profile

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreammind.BuildConfig
import com.example.dreammind.core.data.repository.AuthRepository
import com.example.dreammind.core.data.repository.ProfileRepository
import com.example.dreammind.core.viewmodel.dreamMindViewModelFactory

@Composable
fun ProfileRoute(
    profileRepository: ProfileRepository,
    authRepository: AuthRepository,
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(
        factory = dreamMindViewModelFactory {
            ProfileViewModel(profileRepository, authRepository)
        }
    )
) {
    LaunchedEffect(Unit) {
        viewModel.refreshProfile()
    }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    ProfileScreen(
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        noticeMessage = uiState.noticeMessage,
        profile = uiState.profile,
        versionLabel = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
        onHeaderAction = { viewModel.showSettingNotice("Profile settings") },
        onCameraClick = { viewModel.showSettingNotice("Profile photo") },
        onSettingClick = viewModel::showSettingNotice,
        onReminderToggle = viewModel::toggleBedtimeReminder,
        onLogout = { viewModel.logout(onLoggedOut) }
    )
}
