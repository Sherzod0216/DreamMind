package com.example.dreammind.feature.home

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreammind.core.data.repository.SleepSessionRepository
import com.example.dreammind.core.navigation.BottomNavItem
import com.example.dreammind.core.viewmodel.dreamMindViewModelFactory

@Composable
fun HomeRoute(
    sleepSessionRepository: SleepSessionRepository,
    onActionSelected: (BottomNavItem) -> Unit,
    onOpenSleepLog: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSessionDetail: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = dreamMindViewModelFactory {
            HomeViewModel(sleepSessionRepository)
        }
    )
) {
    LaunchedEffect(Unit) {
        viewModel.refreshLatestSession()
    }

    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val dashboard = uiState.dashboard

    HomeScreen(
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        hasSleepData = uiState.hasSleepData,
        userName = dashboard.userName,
        greeting = dashboard.greeting,
        lastSleepDuration = dashboard.lastSleepDuration,
        sleepScore = dashboard.sleepScore,
        insightTitle = dashboard.insightTitle,
        insightBody = dashboard.insightBody,
        routineItems = dashboard.routineItems,
        onActionSelected = onActionSelected,
        onOpenSleepLog = onOpenSleepLog,
        onOpenProfile = onOpenProfile,
        onOpenRoutine = { onActionSelected(BottomNavItem.Coach) },
        onOpenSessionDetail = {
            onOpenSessionDetail(uiState.latestSessionId ?: "latest")
        }
    )
}
