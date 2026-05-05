package com.example.dreammind.feature.coach

import com.example.dreammind.MainDispatcherRule
import com.example.dreammind.core.common.AppResult
import com.example.dreammind.core.data.repository.CoachRepository
import com.example.dreammind.data.CoachMessage
import com.example.dreammind.data.CoachState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class CoachViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun sendDraftMessage_clearsDraftAndAppendsExchange() = runTest {
        val repository = FakeCoachRepository()
        val viewModel = CoachViewModel(repository)

        viewModel.updateDraft("Why did I sleep better?")
        viewModel.sendDraftMessage()

        val state = viewModel.uiState.value
        assertEquals("", state.draftMessage)
        assertFalse(state.isSending)
        assertEquals(2, state.coach.messages.size)
        assertEquals("Why did I sleep better?", state.coach.messages[0].text)
        assertEquals("Earlier bedtime helped your recovery.", state.coach.messages[1].text)
    }
}

private class FakeCoachRepository : CoachRepository {
    private val _coach = MutableStateFlow(
        CoachState(
            title = "Sleep Coach",
            subtitle = "Testing",
            messages = emptyList(),
            suggestions = listOf("Analyze last night")
        )
    )

    override val coach: StateFlow<CoachState> = _coach

    override suspend fun refreshMessages(): AppResult<Unit> {
        return AppResult.Success(Unit)
    }

    override suspend fun sendMessage(content: String): AppResult<Unit> {
        _coach.value = _coach.value.copy(
            messages = listOf(
                CoachMessage(text = content, time = "Now", fromCoach = false),
                CoachMessage(text = "Earlier bedtime helped your recovery.", time = "Now", fromCoach = true)
            )
        )
        return AppResult.Success(Unit)
    }

    override suspend fun analyzeLastNight(): AppResult<Unit> {
        _coach.value = _coach.value.copy(
            messages = _coach.value.messages + CoachMessage(
                text = "Last night looked strong.",
                time = "Now",
                fromCoach = true
            )
        )
        return AppResult.Success(Unit)
    }
}
