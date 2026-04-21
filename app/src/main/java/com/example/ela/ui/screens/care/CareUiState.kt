package com.example.ela.ui.screens.care

import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase

data class CareActionUiState(
    val isLoading: Boolean = false,
    val actions: List<CareAction> = emptyList(),
    val phase: CyclePhase? = null,
    val error: String? = null
)