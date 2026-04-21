package com.example.ela.ui.screens.home

import com.example.ela.domain.model.CycleInfo

data class HomeUiState(
    val isLoading: Boolean = true,
    val cycleInfo: CycleInfo? = null,
    val error: String? = null
)