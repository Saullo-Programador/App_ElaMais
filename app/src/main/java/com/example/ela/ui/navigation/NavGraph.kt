package com.example.ela.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {

    @Serializable
    object Splash : Screen("splash")

    @Serializable
    object Home : Screen("home")

    @Serializable
    object Login : Screen("login")

    @Serializable
    object Signup : Screen("signup")

    @Serializable
    object Care : Screen("care/{phase}") {
        fun createRoute(phase: String) = "care/$phase"
    }

    @Serializable
    object Cycle : Screen("cycle")
    @Serializable
    object Reminder : Screen("reminder")
    @Serializable
    object Settings : Screen("settings")
    @Serializable
    object Preferences : Screen("preferences")
    @Serializable
    object ManageCare : Screen("manege_care")
}