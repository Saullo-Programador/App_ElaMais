package com.example.ela.ui.screens.importantDate

import com.example.ela.domain.model.ImportantDate

data class ImportantDateUiState(
    val dates: List<ImportantDate> = emptyList()
)