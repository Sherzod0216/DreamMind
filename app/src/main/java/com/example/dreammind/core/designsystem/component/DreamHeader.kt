package com.example.dreammind.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.dreammind.ui.theme.DreamBorder
import com.example.dreammind.ui.theme.DreamCardAlt
import com.example.dreammind.ui.theme.DreamMuted
import com.example.dreammind.ui.theme.DreamPrimary
import com.example.dreammind.ui.theme.DreamPrimarySoft
import com.example.dreammind.ui.theme.DreamText

enum class HeaderActionStyle {
    Avatar,
    Button,
    Highlight
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    actionIcon: ImageVector,
    actionStyle: HeaderActionStyle,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = DreamText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = DreamMuted
            )
        }

        when (actionStyle) {
            HeaderActionStyle.Avatar -> HeaderActionIcon(
                actionIcon = actionIcon,
                shape = CircleShape,
                tint = DreamText,
                color = DreamCardAlt.copy(alpha = 0.92f),
                borderColor = DreamBorder.copy(alpha = 0.5f),
                onClick = onActionClick
            )

            HeaderActionStyle.Button -> HeaderActionIcon(
                actionIcon = actionIcon,
                shape = RoundedCornerShape(16.dp),
                tint = DreamMuted,
                color = DreamCardAlt.copy(alpha = 0.92f),
                borderColor = DreamBorder.copy(alpha = 0.5f),
                onClick = onActionClick
            )

            HeaderActionStyle.Highlight -> HeaderActionIcon(
                actionIcon = actionIcon,
                shape = RoundedCornerShape(16.dp),
                tint = DreamPrimarySoft,
                color = DreamPrimary.copy(alpha = 0.16f),
                borderColor = DreamPrimary.copy(alpha = 0.28f),
                onClick = onActionClick
            )
        }
    }
}

@Composable
private fun HeaderActionIcon(
    actionIcon: ImageVector,
    shape: androidx.compose.ui.graphics.Shape,
    tint: androidx.compose.ui.graphics.Color,
    color: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)?
) {
    Surface(
        modifier = Modifier
            .size(42.dp)
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(shape)
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = color,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = actionIcon,
                contentDescription = null,
                tint = tint
            )
        }
    }
}
