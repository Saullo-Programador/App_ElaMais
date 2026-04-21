package com.example.ela.ui.screens.reminder

import com.example.ela.domain.model.Reminder

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true
)