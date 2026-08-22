package com.example.ela.domain.repository

import com.example.ela.domain.model.Preferences
import kotlinx.coroutines.flow.Flow


interface PreferencesRepository {
    fun getPreferences(): Flow<Preferences?>

    suspend fun savePreferences(preferences: Preferences)

    suspend fun syncPreferences()

    suspend fun updateDarkMode(isDarkMode: Boolean)
}