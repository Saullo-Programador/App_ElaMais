package com.example.ela.ui.navigation

sealed class Screen(val route: String) {

    object Home : Screen("home")

    object Care : Screen("care/{phase}") {
        fun createRoute(phase: String) = "care/$phase"
    }

    object Cycle : Screen("cycle")
    object Reminder : Screen("reminder")
    object Settings : Screen("settings")
    object Preferences : Screen("preferences")
}