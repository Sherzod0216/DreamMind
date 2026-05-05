package com.example.dreammind.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    Home(label = "Home", icon = Icons.Rounded.DarkMode, route = DreamMindRoute.Home.route),
    Alarm(label = "Alarm", icon = Icons.Rounded.Alarm, route = DreamMindRoute.Alarm.route),
    Stats(label = "Stats", icon = Icons.Rounded.BarChart, route = DreamMindRoute.Stats.route),
    Coach(label = "Coach", icon = Icons.Rounded.AutoAwesome, route = DreamMindRoute.Coach.route),
    Profile(label = "Profile", icon = Icons.Rounded.Person, route = DreamMindRoute.Profile.route)
}
