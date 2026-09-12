package com.example.ela.ui.screens.cycle

import com.example.ela.domain.model.Cycle

data class CycleUiState(
    val cycle: Cycle? = null,
    val isSaving: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)