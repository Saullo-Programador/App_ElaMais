package com.example.ela.domain.usecase.preferences

import com.example.ela.domain.model.Preferences
import com.example.ela.domain.repository.PreferencesRepository
import javax.inject.Inject

class SavePreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {

    suspend operator fun invoke(preferences: Preferences) {
        repository.savePreferences(preferences)
    }
}