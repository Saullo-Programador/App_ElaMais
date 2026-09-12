package com.example.ela.domain.usecase.reminder

import com.example.ela.domain.model.Reminder
import com.example.ela.domain.repository.ReminderRepository
import javax.inject.Inject

class DeleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder) {
        repository.deleteReminder(reminder)
    }
}