package com.example.ela.data.remote.dto

data class PreferencesDto(
    val id: String = "",
    val favoriteFoods: List<String> = emptyList(),
    val favoriteSweets: List<String> = emptyList(),
    val dislikedThings: List<String> = emptyList(),
    val symptoms: List<String> = emptyList()
)