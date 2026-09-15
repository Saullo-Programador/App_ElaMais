package com.example.ela.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Preferences(
    val id: Long = 0,
    val favoriteFoods: List<String> = emptyList(),
    val favoriteSweets: List<String> = emptyList(),
    val dislikedThings: List<String> = emptyList(),
    val symptoms: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val notificationsTime: String = "08:00",
    val timesPerDay: Int = 1,
    val isDarkMode: Boolean = false
)