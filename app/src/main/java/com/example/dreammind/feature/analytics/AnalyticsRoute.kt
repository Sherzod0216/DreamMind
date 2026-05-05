package com.example.dreammind.feature.analytics

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreammind.core.data.repository.AnalyticsRepository
import com.example.dreammind.core.viewmodel.dreamMindViewModelFactory

@Composable
fun AnalyticsRoute(
    analyticsRepository: AnalyticsRepository,
    onOpenProfile: () -> Unit,
    onOpenSessionDetail: () -> Unit,
    viewModel: AnalyticsViewModel = viewModel(
        factory = dreamMindViewModelFactory {
            AnalyticsViewModel(analyticsRepository)
        }
    )
) {
    LaunchedEffect(Unit) {
        viewModel.refreshAnalytics()
    }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    StatsScreen(
        stats = uiState.stats,
        selectedPeriod = uiState.selectedPeriod,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onPeriodSelected = viewModel::selectPeriod,
        onOpenProfile = onOpenProfile,
        onOpenSessionDetail = onOpenSessionDetail
    )
}
