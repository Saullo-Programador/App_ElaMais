package com.example.ela.data.remote.dto

data class PreferencesDto(
    val id: String = "",
    val favoriteFoods: List<String> = emptyList(),
    val favoriteSweets: List<String> = emptyList(),
    val dislikedThings: List<String> = emptyList(),
    val symptoms: List<String> = emptyList(),
    val notificationsEnabled: Boolean = true,
    val notificationsTime: String = "08:00",
    val timesPerDay: Int = 1
)