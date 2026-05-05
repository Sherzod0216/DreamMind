package com.example.dreammind.feature.analytics

import com.example.dreammind.MainDispatcherRule
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.data.repository.AnalyticsRepository
import com.example.dreammind.data.SleepBar
import com.example.dreammind.data.StatHighlight
import com.example.dreammind.data.StatsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AnalyticsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun selectPeriod_refreshesStatsForSelectedRange() = runTest {
        val repository = FakeAnalyticsRepository()
        val viewModel = AnalyticsViewModel(repository)

        viewModel.selectPeriod("Month")

        assertEquals("Month", viewModel.uiState.value.selectedPeriod)
        assertEquals("Month avg", viewModel.uiState.value.stats.averageSleep)
        assertEquals("month", repository.lastRange)
    }
}

private class FakeAnalyticsRepository : AnalyticsRepository {
    private val _stats = MutableStateFlow(stats("Week"))

    override val stats: StateFlow<StatsState> = _stats
    var lastRange: String? = null

    override suspend fun refreshAnalytics(range: String): AppResult<Unit> {
        lastRange = range.lowercase()
        _stats.value = stats(range)
        return AppResult.Success(Unit)
    }

    private fun stats(label: String): StatsState {
        return StatsState(
            averageSleep = "$label avg",
            qualityPercent = 88,
            qualityTrend = "$label trend",
            bars = listOf(SleepBar(label = "MON", progress = 0.8f)),
            highlights = listOf(StatHighlight(label = "Consistency", value = "90%"))
        )
    }
}
