package com.example.ela.ui.screens.reminder

import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.model.Reminder

enum class ReminderFilterType {
    ALL, REMINDERS, SPECIAL_DATES
}

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val importantDates: List<ImportantDate> = emptyList(),
    val filteredEvents: List<TimelineItem> = emptyList(),
    val searchQuery: String = "",
    val filterType: ReminderFilterType = ReminderFilterType.ALL,
    val isLoading: Boolean = true,
    val success: String? = null,
    val error: String? = null
)