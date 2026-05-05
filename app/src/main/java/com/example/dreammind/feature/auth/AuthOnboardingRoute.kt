package com.example.dreammind.feature.auth

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreammind.AuthOnboardingFlow
import com.example.dreammind.EntryStage
import com.example.dreammind.core.data.repository.AuthRepository
import com.example.dreammind.core.data.repository.OnboardingRepository
import com.example.dreammind.core.viewmodel.dreamMindViewModelFactory

@Composable
fun AuthOnboardingRoute(
    authRepository: AuthRepository,
    onboardingRepository: OnboardingRepository,
    currentStage: EntryStage,
    onStageChange: (EntryStage) -> Unit,
    onFinish: (Boolean) -> Unit,
    viewModel: AuthOnboardingViewModel = viewModel(
        factory = dreamMindViewModelFactory {
            AuthOnboardingViewModel(authRepository, onboardingRepository)
        }
    )
) {
    AuthOnboardingFlow(
        currentStage = currentStage,
        onStageChange = onStageChange,
        onFinish = onFinish,
        viewModel = viewModel
    )
}
