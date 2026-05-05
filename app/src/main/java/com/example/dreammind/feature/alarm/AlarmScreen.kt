package com.example.dreammind.feature.alarm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.dreammind.core.designsystem.component.GlassCard
import com.example.dreammind.core.designsystem.component.HeaderActionStyle
import com.example.dreammind.core.designsystem.component.PrimaryButton
import com.example.dreammind.core.designsystem.component.ScreenHeader
import com.example.dreammind.core.designsystem.component.SectionTitle
import com.example.dreammind.data.AlarmSound
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
fun AlarmScreen(
    time: String,
    windowLabel: String,
    smartWakeEnabled: Boolean,
    soundOptions: List<AlarmSound>,
    vibrationLevel: Float,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onTimeClick: () -> Unit = {},
    onSmartWakeChanged: (Boolean) -> Unit = {},
    onSmartWakeWindowClick: () -> Unit = {},
    onSoundSelected: (AlarmSound) -> Unit = {},
    onVibrationClick: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ScreenHeader(
                title = "Smart Alarm",
                subtitle = windowLabel,
                actionIcon = Icons.Rounded.Settings,
                actionStyle = HeaderActionStyle.Button,
                onActionClick = onSmartWakeWindowClick
            )
        }

        item {
            AlarmRingCard(
                time = time,
                onClick = onTimeClick
            )
        }

        item {
            SwitchCard(
                title = "Smart Wake",
                body = "Wake up gently during your lightest sleep phase inside the selected smart window.",
                checked = smartWakeEnabled,
                onCheckedChange = onSmartWakeChanged
            )
        }

        item {
            SmartWakeWindowCard(
                windowLabel = windowLabel,
                onClick = onSmartWakeWindowClick
            )
        }

        item {
            SectionTitle(title = "Alarm Sound")
        }

        items(soundOptions) { sound ->
            AlarmSoundRow(
                sound = sound,
                onClick = { onSoundSelected(sound) }
            )
        }

        item {
            VibrationCard(
                level = vibrationLevel,
                onClick = onVibrationClick
            )
        }

        if (errorMessage != null) {
            item {
                AlarmErrorCard(message = errorMessage)
            }
        }

        item {
            PrimaryButton(
                label = if (isSaving) "Saving Alarm..." else "Set Alarm",
                onClick = if (isSaving) null else onSave
            )
        }
    }
}

@Composable
private fun AlarmRingCard(
    time: String,
    onClick: () -> Unit
) {
    GlassCard(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = DreamCardAlt,
                        radius = size.minDimension / 2.15f,
                        style = Stroke(width = 18f)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(DreamPrimary, DreamPrimarySoft, DreamAccent, DreamPrimary)
                        ),
                        startAngle = -90f,
                        sweepAngle = 278f,
                        useCenter = false,
                        style = Stroke(width = 18f, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = time.toDisplayClock(),
                        style = MaterialTheme.typography.displayMedium,
                        color = DreamText
                    )
                    Text(
                        text = time.toMeridiem(),
                        style = MaterialTheme.typography.labelLarge,
                        color = DreamPrimarySoft
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to adjust",
                        style = MaterialTheme.typography.bodySmall,
                        color = DreamMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                        .size(24.dp)
                        .background(DreamText, CircleShape)
                        .border(4.dp, DreamPrimary, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun SmartWakeWindowCard(
    windowLabel: String,
    onClick: () -> Unit
) {
    GlassCard(onClick = onClick) {
        Text(text = "Smart Wake Window", color = DreamText, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = windowLabel, color = DreamMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Tap to cycle 15 / 30 / 45 minutes", color = DreamPrimarySoft, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SwitchCard(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = DreamText, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = body, color = DreamMuted, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DreamText,
                    checkedTrackColor = DreamPrimary,
                    uncheckedThumbColor = DreamText,
                    uncheckedTrackColor = DreamCardAlt
                )
            )
        }
    }
}

@Composable
private fun AlarmSoundRow(
    sound: AlarmSound,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (sound.selected) DreamCardAlt.copy(alpha = 0.92f) else DreamCard.copy(alpha = 0.76f),
        border = BorderStroke(
            1.dp,
            if (sound.selected) DreamPrimary.copy(alpha = 0.35f) else DreamBorder.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(DreamSurface.copy(alpha = 0.9f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = when (sound.title) {
                        "Forest Morning" -> Icons.Rounded.AutoAwesome
                        "Ocean Swell" -> Icons.Rounded.WaterDrop
                        else -> Icons.Rounded.MusicNote
                    },
                    contentDescription = null,
                    tint = if (sound.selected) DreamPrimarySoft else DreamMuted
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = sound.title, color = DreamText, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = sound.subtitle, color = DreamMuted, style = MaterialTheme.typography.bodySmall)
            }
            SelectionDot(selected = sound.selected)
        }
    }
}

@Composable
private fun AlarmErrorCard(message: String) {
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
private fun SelectionDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(
                width = 2.dp,
                color = if (selected) DreamPrimary else DreamBorder,
                shape = CircleShape
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DreamPrimary, CircleShape)
            )
        }
    }
}

@Composable
private fun VibrationCard(
    level: Float,
    onClick: () -> Unit
) {
    GlassCard(onClick = onClick) {
        Text(text = "Vibration Intensity", color = DreamText, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(DreamSurface.copy(alpha = 0.9f), RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(level)
                    .height(8.dp)
                    .background(
                        brush = Brush.horizontalGradient(listOf(DreamPrimary, DreamAccent)),
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "None", color = DreamMuted, style = MaterialTheme.typography.bodySmall)
            Text(text = "Tap to adjust", color = DreamMuted, style = MaterialTheme.typography.bodySmall)
            Text(text = "Strong", color = DreamPrimarySoft, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun String.toDisplayClock(): String {
    val parts = split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return this
    val minute = parts.getOrNull(1) ?: "00"
    val displayHour = when (val normalized = hour % 12) {
        0 -> 12
        else -> normalized
    }
    return "$displayHour:$minute"
}

private fun String.toMeridiem(): String {
    val hour = split(":").getOrNull(0)?.toIntOrNull() ?: return ""
    return if (hour >= 12) "PM" else "AM"
}
