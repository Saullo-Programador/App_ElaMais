package com.example.ela.ui.screens.cycleHistory

import com.example.ela.domain.model.CycleRecord

data class CycleHistoryUiState(
    val history: List<CycleRecord> = emptyList(),
    val isLoading: Boolean = true
)