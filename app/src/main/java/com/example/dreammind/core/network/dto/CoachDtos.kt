package com.example.dreammind.core.network.dto

import com.example.dreammind.data.CoachMessage
import com.example.dreammind.data.CoachState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CoachMessagesResponse(
    val items: List<CoachMessageResponse>
)

data class CoachMessageResponse(
    val id: String? = null,
    val role: String,
    val content: String,
    val createdAt: String? = null
)

data class SendCoachMessageRequest(
    val content: String
)

data class CoachExchangeResponse(
    val userMessage: CoachMessageResponse,
    val assistantMessage: CoachMessageResponse
)

data class AnalyzeLastNightResponse(
    val message: CoachMessageResponse
)

fun CoachMessagesResponse.toUiState(fallback: CoachState): CoachState {
    return fallback.copy(
        messages = items.map { it.toUiMessage() }.ifEmpty { fallback.messages }
    )
}

fun CoachMessageResponse.toUiMessage(): CoachMessage {
    return CoachMessage(
        text = content,
        time = createdAt.toDisplayTime(),
        fromCoach = role.equals("ASSISTANT", ignoreCase = true)
    )
}

fun nowCoachMessage(
    text: String,
    fromCoach: Boolean
): CoachMessage {
    return CoachMessage(
        text = text,
        time = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
        fromCoach = fromCoach
    )
}

private fun String?.toDisplayTime(): String {
    if (isNullOrBlank()) {
        return SimpleDateFormat("hh:mm a", Locale.US).format(Date())
    }

    return parseIsoDate()?.let {
        SimpleDateFormat("hh:mm a", Locale.US).format(it)
    } ?: this
}

private fun String.parseIsoDate(): Date? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    return patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).parse(this)
        }.getOrNull()
    }
}
