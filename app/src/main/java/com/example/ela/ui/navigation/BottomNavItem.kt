package com.example.ela.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        icon = Icons.Outlined.Home,
        route = Screen.Home.route
    ),
    BottomNavItem(
        label = "Cuidados",
        icon = Icons.Outlined.FavoriteBorder,
        route = Screen.Care.route
    ),
    BottomNavItem(
        label = "Ciclo",
        icon = Icons.Outlined.CalendarMonth,
        route = Screen.Cycle.route
    ),
    BottomNavItem(
        label = "Lembretes",
        icon = Icons.Outlined.Notifications,
        route = Screen.Reminder.route
    ),
    BottomNavItem(
        label = "Config",
        icon = Icons.Outlined.Settings,
        route = Screen.Settings.route
    )
)