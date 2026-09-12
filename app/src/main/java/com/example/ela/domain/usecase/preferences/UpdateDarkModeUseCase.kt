package com.example.ela.domain.usecase.preferences

import com.example.ela.domain.repository.PreferencesRepository
import javax.inject.Inject

class UpdateDarkModeUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(isDarkMode: Boolean){
        repository.updateDarkMode(isDarkMode)
    }
}