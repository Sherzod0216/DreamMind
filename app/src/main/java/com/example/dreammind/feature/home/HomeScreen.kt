package com.example.dreammind.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dreammind.core.designsystem.component.GlassCard
import com.example.dreammind.core.designsystem.component.HeaderActionStyle
import com.example.dreammind.core.designsystem.component.HeroIcon
import com.example.dreammind.core.designsystem.component.PrimaryButton
import com.example.dreammind.core.designsystem.component.ScorePill
import com.example.dreammind.core.designsystem.component.ScreenHeader
import com.example.dreammind.core.designsystem.component.SectionTitle
import com.example.dreammind.core.navigation.BottomNavItem
import com.example.dreammind.data.RoutineItem
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
fun HomeScreen(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    hasSleepData: Boolean,
    userName: String,
    greeting: String,
    lastSleepDuration: String,
    sleepScore: Int,
    insightTitle: String,
    insightBody: String,
    routineItems: List<RoutineItem>,
    onActionSelected: (BottomNavItem) -> Unit,
    onOpenSleepLog: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRoutine: () -> Unit,
    onOpenSessionDetail: () -> Unit
) {
    if (!hasSleepData) {
        EmptyDashboardScreen(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onOpenProfile = onOpenProfile,
            onLogSleep = onOpenSleepLog
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ScreenHeader(
                title = "Good evening, $userName",
                subtitle = greeting,
                actionIcon = Icons.Rounded.Person,
                actionStyle = HeaderActionStyle.Avatar,
                onActionClick = onOpenProfile
            )
        }

        if (errorMessage != null) {
            item {
                HomeErrorCard(message = errorMessage)
            }
        }

        item {
            HeroSleepCard(
                duration = lastSleepDuration,
                score = sleepScore,
                onClick = onOpenSessionDetail
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Hotel,
                    label = "Sleep Log",
                    accent = DreamPrimary,
                    onClick = onOpenSleepLog
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Alarm,
                    label = "Set Alarm",
                    accent = DreamAccent,
                    onClick = { onActionSelected(BottomNavItem.Alarm) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.AutoAwesome,
                    label = "AI Coach",
                    accent = DreamPrimarySoft,
                    onClick = { onActionSelected(BottomNavItem.Coach) }
                )
            }
        }

        item {
            InsightCard(
                title = insightTitle,
                body = insightBody
            )
        }

        item {
            SectionTitle(
                title = "Nightly Routine",
                action = "View all",
                onActionClick = onOpenRoutine
            )
        }

        items(routineItems) { item ->
            RoutineRow(
                item = item,
                onClick = onOpenRoutine
            )
        }
    }
}

@Composable
private fun EmptyDashboardScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onOpenProfile: () -> Unit,
    onLogSleep: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome Hero",
                    style = MaterialTheme.typography.labelLarge,
                    color = DreamMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Dashboard",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DreamText
                )
            }
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(onClick = onOpenProfile),
                shape = RoundedCornerShape(18.dp),
                color = DreamCardAlt.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.4f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = DreamText.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeroIcon(
                icon = Icons.Rounded.Hotel,
                accentIcon = Icons.Rounded.AutoAwesome,
                haloSize = 180.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isLoading) "Loading your latest night" else "A New Beginning",
                style = MaterialTheme.typography.headlineSmall,
                color = DreamText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage
                    ?: "Log your first night to unlock deep sleep insights and personalized recovery metrics.",
                style = MaterialTheme.typography.bodyLarge,
                color = DreamMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(22.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = DreamCardAlt.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = DreamPrimarySoft,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Takes only 30 seconds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DreamText.copy(alpha = 0.92f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 18.dp)
        ) {
            PrimaryButton(
                label = "Log Sleep Session",
                onClick = onLogSleep
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(128.dp)
                        .height(5.dp)
                        .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
                )
            }
        }
    }
}

@Composable
private fun HomeErrorCard(message: String) {
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
private fun HeroSleepCard(
    duration: String,
    score: Int,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Last Night's Sleep",
                        style = MaterialTheme.typography.labelMedium,
                        color = DreamMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.displaySmall,
                        color = DreamText
                    )
                }

                ScorePill(score = score)
            }

            Spacer(modifier = Modifier.height(24.dp))
            SleepWaveform()
        }
    }
}

@Composable
private fun SleepWaveform() {
    val bars = listOf(0.35f, 0.55f, 0.28f, 0.78f, 0.94f, 0.58f, 0.36f, 0.18f, 0.42f, 0.74f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEachIndexed { index, value ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((value * 84f).dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (index in 3..5) {
                                listOf(DreamPrimarySoft, DreamPrimary)
                            } else {
                                listOf(DreamPrimary.copy(alpha = 0.35f), DreamPrimary.copy(alpha = 0.72f))
                            }
                        )
                    )
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = DreamCardAlt.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accent.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                color = DreamText,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    body: String
) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DreamText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DreamMuted
                )
            }
        }
    }
}

@Composable
private fun RoutineRow(
    item: RoutineItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = DreamCardAlt.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(DreamCard.copy(alpha = 0.82f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.title.contains("Rain")) Icons.Rounded.MusicNote else Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = DreamPrimarySoft
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, color = DreamText, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = item.subtitle, color = DreamMuted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                imageVector = if (item.title.contains("Rain")) Icons.Rounded.PlayArrow else Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = DreamMuted
            )
        }
    }
}
