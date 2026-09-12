package com.example.ela.ui.screens.home

import com.example.ela.domain.model.CycleInfo
import com.example.ela.domain.model.CycleCalendarDates

data class HomeUiState(
    val isLoading: Boolean = true,
    val cycleInfo: CycleInfo? = null,
    val calendarDates: CycleCalendarDates? = null,
    val error: String? = null
)