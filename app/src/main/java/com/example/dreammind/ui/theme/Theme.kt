package com.example.dreammind.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DreamColorScheme = darkColorScheme(
    primary = DreamPrimary,
    onPrimary = DeepNight,
    secondary = DreamCardAlt,
    onSecondary = DreamText,
    tertiary = DreamAccent,
    background = DeepNight,
    onBackground = DreamText,
    surface = DreamCard,
    onSurface = DreamText,
    surfaceVariant = DreamCardAlt,
    outline = DreamBorder,
    error = DreamDanger
)

@Composable
fun DreamMindTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DreamColorScheme,
        typography = Typography,
        content = content
    )
}
