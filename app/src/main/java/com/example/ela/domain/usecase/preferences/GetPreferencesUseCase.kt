package com.example.ela.domain.usecase.preferences

import com.example.ela.domain.repository.PreferencesRepository
import javax.inject.Inject

class GetPreferencesUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    operator fun invoke() = repository.getPreferences()
}