package com.example.dreammind.core.navigation

sealed interface DreamMindRoute {
    val route: String

    data object Welcome : DreamMindRoute {
        override val route = "auth/welcome"
    }

    data object CreateAccount : DreamMindRoute {
        override val route = "auth/create-account"
    }

    data object SignIn : DreamMindRoute {
        override val route = "auth/sign-in"
    }

    data object Goal : DreamMindRoute {
        override val route = "onboarding/goal"
    }

    data object Schedule : DreamMindRoute {
        override val route = "onboarding/schedule"
    }

    data object Sync : DreamMindRoute {
        override val route = "onboarding/sync"
    }

    data object Home : DreamMindRoute {
        override val route = "main/home"
    }

    data object Alarm : DreamMindRoute {
        override val route = "main/alarm"
    }

    data object Stats : DreamMindRoute {
        override val route = "main/stats"
    }

    data object Coach : DreamMindRoute {
        override val route = "main/coach"
    }

    data object Profile : DreamMindRoute {
        override val route = "main/profile"
    }

    data object SleepLog : DreamMindRoute {
        override val route = "main/sleep-log"
    }

    data object SessionDetail : DreamMindRoute {
        const val SESSION_ID = "sessionId"
        override val route = "main/session-detail/{$SESSION_ID}"

        fun createRoute(sessionId: String) = "main/session-detail/$sessionId"
    }
}
