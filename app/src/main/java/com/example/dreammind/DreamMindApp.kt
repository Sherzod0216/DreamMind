package com.example.dreammind

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.dreammind.core.data.fake.FakeDreamMindRepository
import com.example.dreammind.core.data.repository.AlarmRepository
import com.example.dreammind.core.data.repository.AnalyticsRepository
import com.example.dreammind.core.data.repository.AuthRepository
import com.example.dreammind.core.data.repository.CoachRepository
import com.example.dreammind.core.data.repository.DreamMindRepository
import com.example.dreammind.core.data.repository.OnboardingRepository
import com.example.dreammind.core.data.repository.ProfileRepository
import com.example.dreammind.core.data.repository.RemoteAlarmRepository
import com.example.dreammind.core.data.repository.RemoteAnalyticsRepository
import com.example.dreammind.core.data.repository.RemoteAuthRepository
import com.example.dreammind.core.data.repository.RemoteCoachRepository
import com.example.dreammind.core.data.repository.RemoteOnboardingRepository
import com.example.dreammind.core.data.repository.RemoteProfileRepository
import com.example.dreammind.core.data.repository.RemoteSleepSessionRepository
import com.example.dreammind.core.data.repository.SleepSessionRepository
import com.example.dreammind.core.datastore.AuthTokenStore
import com.example.dreammind.core.designsystem.component.DreamBackdrop
import com.example.dreammind.core.network.DreamMindNetwork
import com.example.dreammind.core.navigation.DreamMindNavGraph
import com.example.dreammind.ui.theme.DeepNight
import com.example.dreammind.ui.theme.DreamMindTheme
import com.example.dreammind.ui.theme.DreamSurface

@Composable
fun DreamMindApp() {
    val context = LocalContext.current.applicationContext
    val repository: DreamMindRepository = remember { FakeDreamMindRepository }
    val tokenStore = remember(context) { AuthTokenStore(context) }
    val api = remember(tokenStore) { DreamMindNetwork.createApi(tokenStore) }
    val authRepository: AuthRepository = remember(api, tokenStore) {
        RemoteAuthRepository(api, tokenStore)
    }
    val onboardingRepository: OnboardingRepository = remember(api) {
        RemoteOnboardingRepository(api)
    }
    val sleepSessionRepository: SleepSessionRepository = remember(api, repository) {
        RemoteSleepSessionRepository(api, repository)
    }
    val alarmRepository: AlarmRepository = remember(api, repository) {
        RemoteAlarmRepository(api, repository)
    }
    val analyticsRepository: AnalyticsRepository = remember(api, repository) {
        RemoteAnalyticsRepository(api, repository)
    }
    val coachRepository: CoachRepository = remember(api, repository) {
        RemoteCoachRepository(api, repository)
    }
    val profileRepository: ProfileRepository = remember(api, repository) {
        RemoteProfileRepository(api, repository)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepNight, DreamSurface, DeepNight)
                )
            )
    ) {
        DreamBackdrop()
        DreamMindNavGraph(
            repository = repository,
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            sleepSessionRepository = sleepSessionRepository,
            alarmRepository = alarmRepository,
            analyticsRepository = analyticsRepository,
            coachRepository = coachRepository,
            profileRepository = profileRepository
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DreamMindAppPreview() {
    DreamMindTheme {
        DreamMindApp()
    }
}
