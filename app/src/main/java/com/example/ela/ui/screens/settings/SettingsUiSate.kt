package com.example.ela.ui.screens.settings

import com.example.ela.domain.model.Preferences

data class SettingsUiState(
    val preferences: Preferences = Preferences(),
    val isLoading: Boolean = false,
    val isClearingData: Boolean = false,
    val dataClearedSuccess: Boolean = false,
    val errorMessage: String? = null
)

