package com.example.ela.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class PreferencesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val favoriteFoods: String, // JSON
    val favoriteSweets: String,
    val dislikedThings: String,
    val symptoms: String,
    val notificationsEnabled: Boolean = true,
    val notificationsTime: String = "08:00",
    val timesPerDay: Int = 1
)