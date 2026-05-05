package com.example.dreammind.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dreammind.ui.theme.DreamAccent
import com.example.dreammind.ui.theme.DreamPrimary
import com.example.dreammind.ui.theme.DreamPrimarySoft

@Composable
fun BoxScope.DreamBackdrop() {
    GlowOrb(
        alignment = Alignment.TopEnd,
        size = 280.dp,
        colors = listOf(DreamPrimary.copy(alpha = 0.18f), Color.Transparent),
        xOffset = 90.dp,
        yOffset = (-90).dp
    )
    GlowOrb(
        alignment = Alignment.CenterStart,
        size = 220.dp,
        colors = listOf(DreamAccent.copy(alpha = 0.10f), Color.Transparent),
        xOffset = (-100).dp,
        yOffset = 80.dp
    )
    GlowOrb(
        alignment = Alignment.BottomCenter,
        size = 320.dp,
        colors = listOf(DreamPrimarySoft.copy(alpha = 0.08f), Color.Transparent),
        xOffset = 0.dp,
        yOffset = 120.dp
    )
}

@Composable
private fun BoxScope.GlowOrb(
    alignment: Alignment,
    size: Dp,
    colors: List<Color>,
    xOffset: Dp,
    yOffset: Dp
) {
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(x = xOffset, y = yOffset)
            .size(size)
            .background(
                brush = Brush.radialGradient(colors = colors),
                shape = CircleShape
            )
    )
}
