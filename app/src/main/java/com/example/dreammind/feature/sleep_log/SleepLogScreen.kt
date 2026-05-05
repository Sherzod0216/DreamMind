package com.example.dreammind.feature.sleep_log

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.dreammind.core.designsystem.component.DreamBackdrop
import com.example.dreammind.core.designsystem.component.GlassCard
import com.example.dreammind.core.designsystem.component.PrimaryButton
import com.example.dreammind.core.designsystem.component.SectionTitle
import com.example.dreammind.data.SleepLogState
import com.example.dreammind.ui.theme.DeepNight
import com.example.dreammind.ui.theme.DreamAccent
import com.example.dreammind.ui.theme.DreamBorder
import com.example.dreammind.ui.theme.DreamCard
import com.example.dreammind.ui.theme.DreamCardAlt
import com.example.dreammind.ui.theme.DreamMuted
import com.example.dreammind.ui.theme.DreamPrimary
import com.example.dreammind.ui.theme.DreamPrimarySoft
import com.example.dreammind.ui.theme.DreamSurface
import com.example.dreammind.ui.theme.DreamText

@Composable
fun SleepLogScreen(
    state: SleepLogState,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onBack: () -> Unit,
    onCalendarClick: () -> Unit,
    onBedtimeClick: () -> Unit,
    onWakeTimeClick: () -> Unit,
    onQualityClick: () -> Unit,
    onActivityClick: (String) -> Unit,
    onAddTag: () -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
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
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 40.dp),
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
                    Column {
                        Text(
                            text = "Tonight's Log",
                            style = MaterialTheme.typography.labelLarge,
                            color = DreamMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Activity",
                            style = MaterialTheme.typography.headlineSmall,
                            color = DreamText
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(onClick = onBack),
                            shape = RoundedCornerShape(16.dp),
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

                        Surface(
                            modifier = Modifier
                                .size(42.dp)
                                .clickable(onClick = onCalendarClick),
                            shape = RoundedCornerShape(16.dp),
                            color = DreamCardAlt.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.42f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = DreamPrimarySoft
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    TimeSummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.NightlightRound,
                        title = "Bedtime",
                        value = state.bedtime,
                        onClick = onBedtimeClick
                    )
                    TimeSummaryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.WbTwilight,
                        title = "Wake up",
                        value = state.wakeTime,
                        onClick = onWakeTimeClick
                    )
                }
            }

            item {
                QualityCard(
                    label = state.qualityLabel,
                    level = state.qualityLevel,
                    onClick = onQualityClick
                )
            }

            item {
                SectionTitle(title = "Pre-sleep Activities")
            }

            item {
                ActivityTagWrap(
                    allActivities = state.allActivities,
                    selectedActivities = state.selectedActivities,
                    onActivityClick = onActivityClick,
                    onAddTag = onAddTag
                )
            }

            item {
                NotesCard(
                    value = state.notes,
                    onValueChange = onNotesChange
                )
            }

            if (errorMessage != null) {
                item {
                    SaveErrorCard(message = errorMessage)
                }
            }

            item {
                PrimaryButton(
                    label = if (isSaving) "Saving ${state.sessionDateOffsetDays.toDateLabel()}..." else "Save Sleep Entry",
                    onClick = if (isSaving) null else onSave
                )
            }
        }
    }
}

@Composable
private fun SaveErrorCard(message: String) {
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
private fun TimeSummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = DreamCard.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DreamMuted
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, color = DreamMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = DreamText, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Tap to change", color = DreamMuted.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun QualityCard(
    label: String,
    level: Float,
    onClick: () -> Unit
) {
    GlassCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sleep Quality",
                color = DreamText,
                style = MaterialTheme.typography.titleLarge
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = DreamPrimary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, DreamPrimary.copy(alpha = 0.22f))
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = DreamPrimarySoft,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(DreamCardAlt.copy(alpha = 0.95f), RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(level)
                    .height(10.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(DreamPrimary, DreamAccent)),
                        shape = RoundedCornerShape(999.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(level)
                    .height(10.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(DreamText, CircleShape)
                        .border(4.dp, DreamPrimary, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Restless", color = DreamMuted, style = MaterialTheme.typography.bodySmall)
            Text(text = "Tap to adjust", color = DreamMuted, style = MaterialTheme.typography.bodySmall)
            Text(text = "Peaceful", color = DreamMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ActivityTagWrap(
    allActivities: List<String>,
    selectedActivities: Set<String>,
    onActivityClick: (String) -> Unit,
    onAddTag: () -> Unit
) {
    val chunkedRows = allActivities.chunked(3)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        chunkedRows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { tag ->
                    val selected = selectedActivities.contains(tag)
                    ActivityTag(
                        modifier = Modifier.weight(1f),
                        label = tag,
                        selected = selected,
                        onClick = { onActivityClick(tag) }
                    )
                }
                if (row.size < 3) {
                    AddTagCard(
                        modifier = Modifier.weight(1f),
                        onClick = onAddTag
                    )
                }
            }
        }
        if (allActivities.size % 3 == 0) {
            Row {
                AddTagCard(
                    modifier = Modifier.weight(1f),
                    onClick = onAddTag
                )
                Spacer(modifier = Modifier.weight(2f))
            }
        }
    }
}

@Composable
private fun ActivityTag(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) DreamPrimary.copy(alpha = 0.16f) else DreamCardAlt.copy(alpha = 0.72f),
        border = BorderStroke(
            1.dp,
            if (selected) DreamPrimary.copy(alpha = 0.32f) else DreamBorder.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (label) {
                    "Reading" -> Icons.AutoMirrored.Rounded.MenuBook
                    "Screen Time" -> Icons.Rounded.Tv
                    "Exercise" -> Icons.Rounded.FitnessCenter
                    "Meditation" -> Icons.Rounded.AutoAwesome
                    else -> Icons.Rounded.AutoAwesome
                },
                contentDescription = null,
                tint = if (selected) DreamPrimarySoft else DreamMuted,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = if (selected) DreamText else DreamText.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AddTagCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = DreamCard.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Add tag",
                color = DreamMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun NotesCard(
    value: String,
    onValueChange: (String) -> Unit
) {
    GlassCard {
        Text(
            text = "Notes",
            color = DreamText,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            minLines = 3,
            textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = DreamText)),
            modifier = Modifier
                .fillMaxWidth()
                .background(DreamCardAlt.copy(alpha = 0.72f), RoundedCornerShape(18.dp))
                .padding(14.dp),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = "Anything that affected your sleep?",
                        color = DreamMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                innerTextField()
            }
        )
    }
}

private fun Int.toDateLabel(): String {
    return when (this) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> "$this days ago"
    }
}
