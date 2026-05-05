package com.example.dreammind.feature.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dreammind.core.designsystem.component.GlassCard
import com.example.dreammind.core.designsystem.component.HeaderActionStyle
import com.example.dreammind.core.designsystem.component.ScreenHeader
import com.example.dreammind.core.designsystem.component.SectionTitle
import com.example.dreammind.data.ProfileSetting
import com.example.dreammind.data.ProfileSettingType
import com.example.dreammind.data.ProfileState
import com.example.dreammind.ui.theme.DeepNight
import com.example.dreammind.ui.theme.DreamAccent
import com.example.dreammind.ui.theme.DreamBorder
import com.example.dreammind.ui.theme.DreamCard
import com.example.dreammind.ui.theme.DreamCardAlt
import com.example.dreammind.ui.theme.DreamMuted
import com.example.dreammind.ui.theme.DreamPrimary
import com.example.dreammind.ui.theme.DreamPrimarySoft
import com.example.dreammind.ui.theme.DreamText

@Composable
fun ProfileScreen(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    noticeMessage: String? = null,
    profile: ProfileState,
    versionLabel: String,
    onHeaderAction: () -> Unit,
    onCameraClick: () -> Unit,
    onSettingClick: (String) -> Unit,
    onReminderToggle: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ScreenHeader(
                title = "Profile",
                subtitle = if (isLoading) "Refreshing your profile" else "Manage your sleep journey",
                actionIcon = Icons.Rounded.Settings,
                actionStyle = HeaderActionStyle.Button,
                onActionClick = onHeaderAction
            )
        }

        if (errorMessage != null) {
            item {
                ProfileStatusCard(message = errorMessage)
            }
        }

        if (noticeMessage != null) {
            item {
                ProfileStatusCard(message = noticeMessage)
            }
        }

        item {
            ProfileHeroCard(
                profile = profile,
                onCameraClick = onCameraClick
            )
        }

        item {
            SectionTitle(title = "Account Settings")
        }

        items(profile.settings.take(3)) { setting ->
            ProfileSettingRow(
                setting = setting,
                onClick = { onSettingClick(setting.title) },
                onToggle = onReminderToggle
            )
        }

        item {
            SectionTitle(title = "Privacy & Support")
        }

        items(profile.settings.drop(3)) { setting ->
            ProfileSettingRow(
                setting = setting,
                onClick = { onSettingClick(setting.title) },
                onToggle = onReminderToggle
            )
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout),
                shape = RoundedCornerShape(22.dp),
                color = DreamCardAlt.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        tint = Color(0xFFFF8D8D)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFF8D8D)
                    )
                }
            }
        }

        item {
            Text(
                text = versionLabel,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = DreamMuted.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileStatusCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = DreamCardAlt.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, DreamBorder.copy(alpha = 0.45f))
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
private fun ProfileHeroCard(
    profile: ProfileState,
    onCameraClick: () -> Unit
) {
    GlassCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(DreamPrimarySoft, DreamAccent)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.userName.firstOrNull()?.uppercase() ?: "D",
                        style = MaterialTheme.typography.displaySmall,
                        color = DeepNight
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .clickable(onClick = onCameraClick)
                        .background(DreamPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        tint = DreamText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = profile.userName, color = DreamText, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = profile.memberLabel, color = DreamMuted, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                profile.stats.forEach { stat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stat.value, color = DreamText, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stat.label, color = DreamMuted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSettingRow(
    setting: ProfileSetting,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (setting.type == ProfileSettingType.ToggleOn) {
                    onToggle()
                } else {
                    onClick()
                }
            },
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
                    .background(DreamCard.copy(alpha = 0.82f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = when (setting.title) {
                        "Personal Information" -> Icons.Rounded.Person
                        "Bedtime Reminders" -> Icons.Rounded.Alarm
                        "Connected Devices" -> Icons.Rounded.AutoAwesome
                        "Privacy Policy" -> Icons.Rounded.Shield
                        else -> Icons.AutoMirrored.Rounded.HelpOutline
                    },
                    contentDescription = null,
                    tint = DreamPrimarySoft
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = setting.title, color = DreamText, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = setting.subtitle, color = DreamMuted, style = MaterialTheme.typography.bodySmall)
            }

            when (setting.type) {
                ProfileSettingType.ToggleOn -> Switch(
                    checked = setting.enabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DreamText,
                        checkedTrackColor = DreamPrimary
                    )
                )

                ProfileSettingType.External,
                ProfileSettingType.Link -> androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = DreamMuted
                )
            }
        }
    }
}
