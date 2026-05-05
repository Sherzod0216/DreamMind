package com.example.dreammind.feature.session_detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dreammind.core.designsystem.component.DreamBackdrop
import com.example.dreammind.core.designsystem.component.GlassCard
import com.example.dreammind.core.designsystem.component.ScorePill
import com.example.dreammind.data.SessionDetailState
import com.example.dreammind.data.SleepStageKind
import com.example.dreammind.ui.theme.DeepNight
import com.example.dreammind.ui.theme.DreamBorder
import com.example.dreammind.ui.theme.DreamCard
import com.example.dreammind.ui.theme.DreamCardAlt
import com.example.dreammind.ui.theme.DreamMuted
import com.example.dreammind.ui.theme.DreamPrimary
import com.example.dreammind.ui.theme.DreamPrimarySoft
import com.example.dreammind.ui.theme.DreamSurface
import com.example.dreammind.ui.theme.DreamText

@Composable
fun SleepSessionDetailScreen(
    state: SessionDetailState,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit
) {
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 42.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onBack),
                            shape = CircleShape,
                            color = DreamCardAlt.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.42f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null,
                                    tint = DreamText
                                )
                            }
                        }

                        Text(
                            text = "Sleep Session",
                            style = MaterialTheme.typography.titleLarge,
                            color = DreamText
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = DreamCardAlt.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = state.dateLabel,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DreamMuted
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    DetailStatusCard(message = "Loading this sleep session...")
                }
            }

            if (errorMessage != null) {
                item {
                    DetailStatusCard(message = errorMessage)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Total Duration",
                            style = MaterialTheme.typography.labelLarge,
                            color = DreamMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.duration,
                            style = MaterialTheme.typography.displaySmall,
                            color = DreamText
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Quality Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = DreamMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ScorePill(score = state.score)
                    }
                }
            }

            item {
                SleepArchitectureCard(state = state)
            }

            item {
                CoachInsightBanner(text = state.insight)
            }

            if (state.activities.isNotEmpty()) {
                item {
                    ActivitiesCard(activities = state.activities)
                }
            }

            if (!state.notes.isNullOrBlank()) {
                item {
                    NotesCard(notes = state.notes)
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.metrics.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { metric ->
                                SessionMetricCard(
                                    modifier = Modifier.weight(1f),
                                    label = metric.label,
                                    value = metric.value,
                                    trend = metric.trend,
                                    progress = metric.progress
                                )
                            }
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailStatusCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = DreamCard.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, DreamPrimary.copy(alpha = 0.28f))
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
private fun SleepArchitectureCard(
    state: SessionDetailState
) {
    GlassCard {
        Text(
            text = "Sleep Architecture",
            style = MaterialTheme.typography.titleMedium,
            color = DreamText
        )
        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 36.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DreamBorder.copy(alpha = 0.55f))
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Wake", "REM", "Light", "Deep").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = DreamMuted
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)
                    .padding(start = 36.dp, top = 28.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                state.stages.forEach { stage ->
                    Box(
                        modifier = Modifier
                            .weight(stage.widthFraction)
                            .height((stage.heightFraction * 132f).dp)
                            .background(
                                color = when (stage.kind) {
                                    SleepStageKind.Wake -> Color(0xFFE98DB3)
                                    SleepStageKind.Rem -> Color(0xFF9E86FF)
                                    SleepStageKind.Light -> DreamPrimary.copy(alpha = 0.65f)
                                    SleepStageKind.Deep -> DreamPrimary
                                },
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)
                    .padding(start = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = state.timelineStart, style = MaterialTheme.typography.bodySmall, color = DreamMuted)
                Text(text = state.timelineMiddle, style = MaterialTheme.typography.bodySmall, color = DreamMuted)
                Text(text = state.timelineEnd, style = MaterialTheme.typography.bodySmall, color = DreamMuted)
            }
        }
    }
}

@Composable
private fun CoachInsightBanner(
    text: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = DreamCard.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, DreamPrimary.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DreamPrimary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = DreamPrimarySoft
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Coach Insight",
                        style = MaterialTheme.typography.titleMedium,
                        color = DreamText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DreamPrimary.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "AI",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = DreamPrimarySoft
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DreamMuted
                )
            }
        }
    }
}

@Composable
private fun ActivitiesCard(activities: List<String>) {
    GlassCard {
        Text(
            text = "Pre-sleep Activities",
            style = MaterialTheme.typography.titleMedium,
            color = DreamText
        )
        Spacer(modifier = Modifier.height(14.dp))
        activities.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { activity ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                        color = DreamPrimary.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, DreamPrimary.copy(alpha = 0.24f))
                    ) {
                        Text(
                            text = activity,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = DreamText
                        )
                    }
                }
                if (row.size < 3) {
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    GlassCard {
        Text(
            text = "Session Notes",
            style = MaterialTheme.typography.titleMedium,
            color = DreamText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = notes,
            style = MaterialTheme.typography.bodyMedium,
            color = DreamMuted
        )
    }
}

@Composable
private fun SessionMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    trend: String,
    progress: Float
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = DreamCardAlt.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = DreamMuted)
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, style = MaterialTheme.typography.titleLarge, color = DreamText)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = trend, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7FE3C4))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(DreamBorder.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .background(DreamPrimary, RoundedCornerShape(999.dp))
                )
            }
        }
    }
}
