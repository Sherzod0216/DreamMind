package com.example.dreammind.data

import com.example.dreammind.core.data.repository.DreamMindRepository

data class DashboardState(
    val userName: String,
    val greeting: String,
    val lastSleepDuration: String,
    val sleepScore: Int,
    val insightTitle: String,
    val insightBody: String,
    val routineItems: List<RoutineItem>
)

data class RoutineItem(
    val title: String,
    val subtitle: String
)

data class AlarmState(
    val alarmTime: String,
    val alarmWindow: String,
    val smartWakeEnabled: Boolean,
    val smartWakeWindowMinutes: Int,
    val soundOptions: List<AlarmSound>,
    val vibrationLevel: Float
)

data class AlarmSound(
    val title: String,
    val subtitle: String,
    val selected: Boolean
)

data class SleepLogState(
    val sessionDateOffsetDays: Int,
    val bedtime: String,
    val wakeTime: String,
    val qualityLabel: String,
    val qualityLevel: Float,
    val selectedActivities: Set<String>,
    val allActivities: List<String>,
    val notes: String
)

data class StatsState(
    val averageSleep: String,
    val qualityPercent: Int,
    val qualityTrend: String,
    val bars: List<SleepBar>,
    val highlights: List<StatHighlight>
)

data class SleepBar(
    val label: String,
    val progress: Float
)

data class SessionDetailState(
    val dateLabel: String,
    val duration: String,
    val score: Int,
    val insight: String,
    val timelineStart: String,
    val timelineMiddle: String,
    val timelineEnd: String,
    val stages: List<SleepStageBar>,
    val metrics: List<SessionMetric>,
    val activities: List<String>,
    val notes: String?
)

data class SleepStageBar(
    val kind: SleepStageKind,
    val widthFraction: Float,
    val heightFraction: Float
)

enum class SleepStageKind {
    Wake,
    Rem,
    Light,
    Deep
}

data class SessionMetric(
    val label: String,
    val value: String,
    val trend: String,
    val progress: Float
)

data class StatHighlight(
    val label: String,
    val value: String
)

data class CoachState(
    val title: String,
    val subtitle: String,
    val messages: List<CoachMessage>,
    val suggestions: List<String>
)

data class CoachMessage(
    val text: String,
    val time: String,
    val fromCoach: Boolean
)

data class ProfileState(
    val userName: String,
    val memberLabel: String,
    val stats: List<ProfileStat>,
    val settings: List<ProfileSetting>
)

data class ProfileStat(
    val label: String,
    val value: String
)

data class ProfileSetting(
    val title: String,
    val subtitle: String,
    val type: ProfileSettingType,
    val enabled: Boolean = false
)

enum class ProfileSettingType {
    Link,
    ToggleOn,
    External
}

object FakeDreamMindRepository : DreamMindRepository {
    override val dashboard = DashboardState(
        userName = "Alex",
        greeting = "Ready for a restful night?",
        lastSleepDuration = "7h 24m",
        sleepScore = 82,
        insightTitle = "AI Insight",
        insightBody = "Drinking herbal tea 30 minutes before bed could improve deep sleep by 12%.",
        routineItems = listOf(
            RoutineItem(title = "Breathing Exercise", subtitle = "5 minutes • Guided"),
            RoutineItem(title = "Deep Rain Ambience", subtitle = "Continuous • Audio")
        )
    )

    override val alarm = AlarmState(
        alarmTime = "06:45",
        alarmWindow = "Alarm set for 6:45 AM • in 8h 12m",
        smartWakeEnabled = true,
        smartWakeWindowMinutes = 30,
        soundOptions = listOf(
            AlarmSound(title = "Forest Morning", subtitle = "Soft birds and light wind", selected = true),
            AlarmSound(title = "Ocean Swell", subtitle = "Rolling waves and calm noise", selected = false),
            AlarmSound(title = "Soft Chimes", subtitle = "Minimal and bright", selected = false)
        ),
        vibrationLevel = 0.68f
    )

    override val sleepLog = SleepLogState(
        sessionDateOffsetDays = 0,
        bedtime = "10:45 PM",
        wakeTime = "07:30 AM",
        qualityLabel = "Fair",
        qualityLevel = 0.65f,
        selectedActivities = setOf("Caffeine", "Exercise"),
        allActivities = listOf(
            "Caffeine",
            "Reading",
            "Screen Time",
            "Exercise",
            "Meditation"
        ),
        notes = ""
    )

    override val stats = StatsState(
        averageSleep = "7h 48m",
        qualityPercent = 85,
        qualityTrend = "Your sleep quality improved by 4% compared to last week.",
        bars = listOf(
            SleepBar(label = "MON", progress = 0.75f),
            SleepBar(label = "TUE", progress = 0.85f),
            SleepBar(label = "WED", progress = 0.60f),
            SleepBar(label = "THU", progress = 0.90f),
            SleepBar(label = "FRI", progress = 0.70f)
        ),
        highlights = listOf(
            StatHighlight(label = "Avg Bedtime", value = "11:24 PM"),
            StatHighlight(label = "Avg Wake Time", value = "7:12 AM"),
            StatHighlight(label = "Sleep Debt", value = "-1h 15m"),
            StatHighlight(label = "Consistency", value = "92%")
        )
    )

    override val sessionDetail = SessionDetailState(
        dateLabel = "Oct 24",
        duration = "7h 24m",
        score = 82,
        insight = "Your REM sleep was 18% higher than usual after yesterday's low screen time. Keep this rhythm for better focus tomorrow.",
        timelineStart = "11:30 PM",
        timelineMiddle = "3:00 AM",
        timelineEnd = "7:00 AM",
        stages = listOf(
            SleepStageBar(kind = SleepStageKind.Wake, widthFraction = 0.04f, heightFraction = 1.0f),
            SleepStageBar(kind = SleepStageKind.Rem, widthFraction = 0.12f, heightFraction = 0.66f),
            SleepStageBar(kind = SleepStageKind.Light, widthFraction = 0.15f, heightFraction = 0.33f),
            SleepStageBar(kind = SleepStageKind.Deep, widthFraction = 0.10f, heightFraction = 0.15f),
            SleepStageBar(kind = SleepStageKind.Light, widthFraction = 0.18f, heightFraction = 0.33f),
            SleepStageBar(kind = SleepStageKind.Rem, widthFraction = 0.08f, heightFraction = 0.66f),
            SleepStageBar(kind = SleepStageKind.Deep, widthFraction = 0.20f, heightFraction = 0.15f),
            SleepStageBar(kind = SleepStageKind.Wake, widthFraction = 0.13f, heightFraction = 1.0f)
        ),
        metrics = listOf(
            SessionMetric(label = "Deep Sleep", value = "1h 12m", trend = "+5m", progress = 0.45f),
            SessionMetric(label = "Efficiency", value = "94%", trend = "Optimal", progress = 0.94f)
        ),
        activities = listOf("Reading", "Meditation"),
        notes = "Kept the room cool and avoided late screen time."
    )

    override val coach = CoachState(
        title = "Sleep Coach",
        subtitle = "Always here to help you rest",
        messages = listOf(
            CoachMessage(
                text = "Good morning! Your deep sleep was 15% higher than your weekly average. How are you feeling today?",
                time = "08:32 AM",
                fromCoach = true
            ),
            CoachMessage(
                text = "Surprisingly refreshed. Can you analyze why my sleep was better?",
                time = "08:34 AM",
                fromCoach = false
            ),
            CoachMessage(
                text = "You went to bed earlier than usual and kept your heart-rate variability steady. That combination likely helped your recovery.",
                time = "08:35 AM",
                fromCoach = true
            )
        ),
        suggestions = listOf(
            "Analyze last night",
            "Bedtime tips",
            "Adjust goals"
        )
    )

    override val profile = ProfileState(
        userName = "Alex Johnson",
        memberLabel = "Pro Sleeper • Member since Jan 2024",
        stats = listOf(
            ProfileStat(label = "Day Streak", value = "14"),
            ProfileStat(label = "Avg Sleep", value = "8.2h"),
            ProfileStat(label = "Consistency", value = "94%")
        ),
        settings = listOf(
            ProfileSetting(title = "Personal Information", subtitle = "Height, weight, age", type = ProfileSettingType.Link),
            ProfileSetting(title = "Bedtime Reminders", subtitle = "Daily at 10:30 PM", type = ProfileSettingType.ToggleOn, enabled = true),
            ProfileSetting(title = "Connected Devices", subtitle = "Apple Watch Connected", type = ProfileSettingType.Link),
            ProfileSetting(title = "Privacy Policy", subtitle = "How DreamMind handles sleep data", type = ProfileSettingType.External),
            ProfileSetting(title = "Help Center", subtitle = "FAQs and support", type = ProfileSettingType.Link)
        )
    )
}

object MockDreamMindRepository : DreamMindRepository by FakeDreamMindRepository
