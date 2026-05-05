package com.example.dreammind.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dreammind.EntryStage
import com.example.dreammind.core.data.repository.AlarmRepository
import com.example.dreammind.core.data.repository.AnalyticsRepository
import com.example.dreammind.core.data.repository.AuthRepository
import com.example.dreammind.core.data.repository.CoachRepository
import com.example.dreammind.core.data.repository.DreamMindRepository
import com.example.dreammind.core.data.repository.OnboardingRepository
import com.example.dreammind.core.data.repository.ProfileRepository
import com.example.dreammind.core.data.repository.SleepSessionRepository
import com.example.dreammind.core.designsystem.component.DreamBottomBar
import com.example.dreammind.core.model.AuthStartupState
import com.example.dreammind.core.viewmodel.dreamMindViewModelFactory
import com.example.dreammind.feature.alarm.AlarmRoute
import com.example.dreammind.feature.analytics.AnalyticsRoute
import com.example.dreammind.feature.auth.AuthOnboardingRoute
import com.example.dreammind.feature.auth.AuthOnboardingViewModel
import com.example.dreammind.feature.coach.CoachRoute
import com.example.dreammind.feature.home.HomeRoute
import com.example.dreammind.feature.profile.ProfileRoute
import com.example.dreammind.feature.session_detail.SessionDetailRoute
import com.example.dreammind.feature.sleep_log.SleepLogRoute
import com.example.dreammind.ui.theme.DreamMuted
import com.example.dreammind.ui.theme.DreamText

private const val LATEST_SESSION_ID = "latest"

@Composable
fun DreamMindNavGraph(
    repository: DreamMindRepository,
    authRepository: AuthRepository,
    onboardingRepository: OnboardingRepository,
    sleepSessionRepository: SleepSessionRepository,
    alarmRepository: AlarmRepository,
    analyticsRepository: AnalyticsRepository,
    coachRepository: CoachRepository,
    profileRepository: ProfileRepository,
    modifier: Modifier = Modifier
) {
    val startupState by produceState<AuthStartupState>(
        initialValue = AuthStartupState.Checking,
        key1 = authRepository
    ) {
        value = authRepository.checkStartupSession()
    }

    when (val state = startupState) {
        AuthStartupState.Checking -> AuthStartupLoading(modifier = modifier)
        AuthStartupState.Unauthenticated -> DreamMindNavHost(
            repository = repository,
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            sleepSessionRepository = sleepSessionRepository,
            alarmRepository = alarmRepository,
            analyticsRepository = analyticsRepository,
            coachRepository = coachRepository,
            profileRepository = profileRepository,
            startDestination = DreamMindRoute.Welcome.route,
            modifier = modifier
        )

        is AuthStartupState.Authenticated -> {
            DreamMindNavHost(
                repository = repository,
                authRepository = authRepository,
                onboardingRepository = onboardingRepository,
                sleepSessionRepository = sleepSessionRepository,
                alarmRepository = alarmRepository,
                analyticsRepository = analyticsRepository,
                coachRepository = coachRepository,
                profileRepository = profileRepository,
                startDestination = if (state.user.onboardingCompleted) {
                    DreamMindRoute.Home.route
                } else {
                    DreamMindRoute.Goal.route
                },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun DreamMindNavHost(
    repository: DreamMindRepository,
    authRepository: AuthRepository,
    onboardingRepository: OnboardingRepository,
    sleepSessionRepository: SleepSessionRepository,
    alarmRepository: AlarmRepository,
    analyticsRepository: AnalyticsRepository,
    coachRepository: CoachRepository,
    profileRepository: ProfileRepository,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authViewModel: AuthOnboardingViewModel = viewModel(
        factory = dreamMindViewModelFactory {
            AuthOnboardingViewModel(authRepository, onboardingRepository)
        }
    )

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        authDestination(
            route = DreamMindRoute.Welcome.route,
            stage = EntryStage.Welcome,
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            viewModel = authViewModel,
            navController = navController,
            onMainReady = {}
        )
        authDestination(
            route = DreamMindRoute.CreateAccount.route,
            stage = EntryStage.CreateAccount,
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            viewModel = authViewModel,
            navController = navController,
            onMainReady = {}
        )
        authDestination(
            route = DreamMindRoute.SignIn.route,
            stage = EntryStage.SignIn,
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            viewModel = authViewModel,
            navController = navController,
            onMainReady = {}
        )
        authDestination(
            route = DreamMindRoute.Goal.route,
            stage = EntryStage.Goal,
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            viewModel = authViewModel,
            navController = navController,
            onMainReady = {}
        )
        authDestination(
            route = DreamMindRoute.Schedule.route,
            stage = EntryStage.Schedule,
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            viewModel = authViewModel,
            navController = navController,
            onMainReady = {}
        )
        authDestination(
            route = DreamMindRoute.Sync.route,
            stage = EntryStage.Sync,
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            viewModel = authViewModel,
            navController = navController,
            onMainReady = {}
        )

        composable(DreamMindRoute.Home.route) {
            MainTabScaffold(
                selectedTab = BottomNavItem.Home,
                navController = navController
            ) {
                HomeRoute(
                    sleepSessionRepository = sleepSessionRepository,
                    onActionSelected = { navController.navigateBottomTab(it) },
                    onOpenSleepLog = { navController.navigate(DreamMindRoute.SleepLog.route) },
                    onOpenProfile = { navController.navigateBottomTab(BottomNavItem.Profile) },
                    onOpenSessionDetail = { sessionId ->
                        navController.navigate(DreamMindRoute.SessionDetail.createRoute(sessionId))
                    }
                )
            }
        }

        composable(DreamMindRoute.Alarm.route) {
            MainTabScaffold(
                selectedTab = BottomNavItem.Alarm,
                navController = navController
            ) {
                AlarmRoute(alarmRepository = alarmRepository)
            }
        }

        composable(DreamMindRoute.Stats.route) {
            MainTabScaffold(
                selectedTab = BottomNavItem.Stats,
                navController = navController
            ) {
                AnalyticsRoute(
                    analyticsRepository = analyticsRepository,
                    onOpenProfile = { navController.navigateBottomTab(BottomNavItem.Profile) },
                    onOpenSessionDetail = {
                        navController.navigate(DreamMindRoute.SessionDetail.createRoute(LATEST_SESSION_ID))
                    }
                )
            }
        }

        composable(DreamMindRoute.Coach.route) {
            MainTabScaffold(
                selectedTab = BottomNavItem.Coach,
                navController = navController
            ) {
                CoachRoute(coachRepository = coachRepository)
            }
        }

        composable(DreamMindRoute.Profile.route) {
            MainTabScaffold(
                selectedTab = BottomNavItem.Profile,
                navController = navController
            ) {
                ProfileRoute(
                    profileRepository = profileRepository,
                    authRepository = authRepository,
                    onLoggedOut = {
                        navController.navigate(DreamMindRoute.Welcome.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(DreamMindRoute.SleepLog.route) {
            SleepLogRoute(
                repository = repository,
                sleepSessionRepository = sleepSessionRepository,
                onBack = { navController.popBackStack() },
                onSaved = { sessionId ->
                    navController.navigate(DreamMindRoute.SessionDetail.createRoute(sessionId)) {
                        popUpTo(DreamMindRoute.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(DreamMindRoute.SessionDetail.route) { backStackEntry ->
            val sessionId = backStackEntry.arguments
                ?.getString(DreamMindRoute.SessionDetail.SESSION_ID)
                ?: LATEST_SESSION_ID
            SessionDetailRoute(
                sleepSessionRepository = sleepSessionRepository,
                sessionId = sessionId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun NavGraphBuilder.authDestination(
    route: String,
    stage: EntryStage,
    authRepository: AuthRepository,
    onboardingRepository: OnboardingRepository,
    viewModel: AuthOnboardingViewModel,
    navController: NavController,
    onMainReady: (Boolean) -> Unit
) {
    composable(route) {
        AuthOnboardingRoute(
            authRepository = authRepository,
            onboardingRepository = onboardingRepository,
            currentStage = stage,
            onStageChange = { nextStage ->
                navController.navigate(nextStage.route)
            },
            onFinish = { returningUser ->
                onMainReady(returningUser)
                navController.navigate(DreamMindRoute.Home.route) {
                    popUpTo(DreamMindRoute.Welcome.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            viewModel = viewModel
        )
    }
}

@Composable
private fun AuthStartupLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DreamMind",
            style = MaterialTheme.typography.headlineMedium,
            color = DreamText,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Checking your sleep session...",
            modifier = Modifier.padding(top = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = DreamMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MainTabScaffold(
    selectedTab: BottomNavItem,
    navController: NavController,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            DreamBottomBar(
                selectedTab = selectedTab,
                onTabSelected = navController::navigateBottomTab
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

private fun NavController.navigateBottomTab(tab: BottomNavItem) {
    navigate(tab.route) {
        popUpTo(DreamMindRoute.Home.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private val EntryStage.route: String
    get() = when (this) {
        EntryStage.Welcome -> DreamMindRoute.Welcome.route
        EntryStage.CreateAccount -> DreamMindRoute.CreateAccount.route
        EntryStage.SignIn -> DreamMindRoute.SignIn.route
        EntryStage.Goal -> DreamMindRoute.Goal.route
        EntryStage.Schedule -> DreamMindRoute.Schedule.route
        EntryStage.Sync -> DreamMindRoute.Sync.route
        EntryStage.Main -> DreamMindRoute.Home.route
    }
