package com.example.dreammind.core.data.repository

import com.example.dreammind.data.AlarmState
import com.example.dreammind.data.CoachState
import com.example.dreammind.data.DashboardState
import com.example.dreammind.data.ProfileState
import com.example.dreammind.data.SessionDetailState
import com.example.dreammind.data.SleepLogState
import com.example.dreammind.data.StatsState

interface DreamMindRepository {
    val dashboard: DashboardState
    val alarm: AlarmState
    val stats: StatsState
    val coach: CoachState
    val profile: ProfileState
    val sleepLog: SleepLogState
    val sessionDetail: SessionDetailState
}
