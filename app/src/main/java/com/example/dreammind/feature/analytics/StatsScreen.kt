package com.example.dreammind.feature.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dreammind.core.designsystem.component.GlassCard
import com.example.dreammind.core.designsystem.component.HeaderActionStyle
import com.example.dreammind.core.designsystem.component.ScreenHeader
import com.example.dreammind.core.designsystem.component.SegmentedPicker
import com.example.dreammind.data.SleepBar
import com.example.dreammind.data.StatsState
import com.example.dreammind.ui.theme.DreamAccent
import com.example.dreammind.ui.theme.DreamBorder
import com.example.dreammind.ui.theme.DreamCardAlt
import com.example.dreammind.ui.theme.DreamMuted
import com.example.dreammind.ui.theme.DreamPrimary
import com.example.dreammind.ui.theme.DreamPrimarySoft
import com.example.dreammind.ui.theme.DreamSurface
import com.example.dreammind.ui.theme.DreamText

@Composable
fun StatsScreen(
    stats: StatsState,
    selectedPeriod: String,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onPeriodSelected: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSessionDetail: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ScreenHeader(
                title = "Sleep Analytics",
                subtitle = if (isLoading) "Refreshing your latest trends" else "Trends built from your recent nights",
                actionIcon = Icons.Rounded.Person,
                actionStyle = HeaderActionStyle.Avatar,
                onActionClick = onOpenProfile
            )
        }

        item {
            SegmentedPicker(
                options = listOf("Week", "Month", "Year"),
                selected = selectedPeriod,
                onSelected = onPeriodSelected
            )
        }

        if (errorMessage != null) {
            item {
                AnalyticsErrorCard(message = errorMessage)
            }
        }

        item {
            SleepHoursCard(
                averageSleep = stats.averageSleep,
                bars = stats.bars,
                onClick = onOpenSessionDetail
            )
        }

        item {
            QualityRingCard(
                qualityPercent = stats.qualityPercent,
                trend = stats.qualityTrend,
                onClick = onOpenSessionDetail
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                stats.highlights.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { highlight ->
                            StatMiniCard(
                                modifier = Modifier.weight(1f),
                                label = highlight.label,
                                value = highlight.value
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

@Composable
private fun AnalyticsErrorCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = DreamAccent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, DreamAccent.copy(alpha = 0.30f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            color = DreamText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SleepHoursCard(
    averageSleep: String,
    bars: List<SleepBar>,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        onClick = onClick
    ) {
        Text(text = "Sleep Hours", color = DreamMuted, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "$averageSleep avg", color = DreamText, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            bars.forEach { bar ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bar.label,
                        color = DreamMuted,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(36.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .background(DreamSurface.copy(alpha = 0.9f), RoundedCornerShape(999.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(bar.progress)
                                .height(12.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(DreamPrimarySoft, DreamPrimary)
                                    ),
                                    shape = RoundedCornerShape(999.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QualityRingCard(
    qualityPercent: Int,
    trend: String,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        onClick = onClick
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = DreamCardAlt,
                        radius = size.minDimension / 2.2f,
                        style = Stroke(width = 16f)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(DreamAccent, DreamPrimary, DreamPrimarySoft, DreamAccent)),
                        startAngle = -90f,
                        sweepAngle = 360f * (qualityPercent / 100f),
                        useCenter = false,
                        style = Stroke(width = 16f, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$qualityPercent%", color = DreamText, style = MaterialTheme.typography.displaySmall)
                    Text(text = "Quality", color = DreamMuted, style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = trend,
                color = DreamMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatMiniCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = DreamCardAlt.copy(alpha = 0.68f),
        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = label, color = DreamMuted, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = DreamText, style = MaterialTheme.typography.titleLarge)
        }
    }
}
