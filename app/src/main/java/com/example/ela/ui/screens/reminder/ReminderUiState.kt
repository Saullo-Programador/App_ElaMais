package com.example.ela.ui.screens.reminder

import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.model.Reminder

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val importantDates: List<ImportantDate> = emptyList(),
    val isLoading: Boolean = true,
    val success: String? = null,
    val error: String? = null
)