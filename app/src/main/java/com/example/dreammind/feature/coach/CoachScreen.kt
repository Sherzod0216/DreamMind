package com.example.dreammind.feature.coach

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.dreammind.core.designsystem.component.HeaderActionStyle
import com.example.dreammind.core.designsystem.component.ScreenHeader
import com.example.dreammind.data.CoachMessage
import com.example.dreammind.data.CoachState
import com.example.dreammind.ui.theme.DeepNight
import com.example.dreammind.ui.theme.DreamBorder
import com.example.dreammind.ui.theme.DreamCard
import com.example.dreammind.ui.theme.DreamCardAlt
import com.example.dreammind.ui.theme.DreamMuted
import com.example.dreammind.ui.theme.DreamPrimary
import com.example.dreammind.ui.theme.DreamPrimarySoft
import com.example.dreammind.ui.theme.DreamText

@Composable
fun CoachScreen(
    coach: CoachState,
    draftMessage: String = "",
    isSending: Boolean = false,
    errorMessage: String? = null,
    onDraftChange: (String) -> Unit = {},
    onSend: () -> Unit = {},
    onSuggestionSelected: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    LaunchedEffect(coach.messages.size, isSending, errorMessage) {
        val totalItems = coach.messages.size + (if (errorMessage != null) 1 else 0) + (if (isSending) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 18.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            ScreenHeader(
                title = coach.title,
                subtitle = coach.subtitle,
                actionIcon = Icons.Rounded.AutoAwesome,
                actionStyle = HeaderActionStyle.Highlight
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(coach.messages) { message ->
                ChatBubble(message = message)
            }

            if (errorMessage != null) {
                item {
                    CoachErrorCard(message = errorMessage)
                }
            }

            if (isSending) {
                item {
                    ChatBubble(
                        message = CoachMessage(
                            text = "Thinking through your sleep patterns...",
                            time = "Now",
                            fromCoach = true
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            coach.suggestions.forEach { suggestion ->
                SuggestionChip(
                    label = suggestion,
                    onClick = { onSuggestionSelected(suggestion) }
                )
            }
        }

        MessageComposer(
            value = draftMessage,
            enabled = !isSending,
            onValueChange = onDraftChange,
            onSend = onSend,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun ChatBubble(message: CoachMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.fromCoach) Alignment.Start else Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = if (message.fromCoach) 8.dp else 24.dp,
                topEnd = if (message.fromCoach) 24.dp else 8.dp,
                bottomStart = 24.dp,
                bottomEnd = 24.dp
            ),
            color = if (message.fromCoach) DreamPrimary.copy(alpha = 0.13f) else DreamCardAlt.copy(alpha = 0.82f),
            border = BorderStroke(
                1.dp,
                if (message.fromCoach) DreamPrimary.copy(alpha = 0.2f) else DreamBorder.copy(alpha = 0.35f)
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = DreamText
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message.time,
            style = MaterialTheme.typography.bodySmall,
            color = DreamMuted
        )
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = DreamCardAlt.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.35f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = DreamText
        )
    }
}

@Composable
private fun CoachErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = DreamPrimary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, DreamPrimary.copy(alpha = 0.22f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = DreamText
        )
    }
}

@Composable
private fun MessageComposer(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = DreamCard.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = DreamText)),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = if (enabled) "Ask your coach anything..." else "Coach is responding...",
                            color = DreamMuted,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    innerTextField()
                }
            )
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clickable(enabled = enabled && value.isNotBlank(), onClick = onSend)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                DreamPrimary.copy(alpha = if (enabled) 1f else 0.45f),
                                DreamPrimarySoft.copy(alpha = if (enabled) 1f else 0.45f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = null,
                    tint = DeepNight
                )
            }
        }
    }
}
