package com.example.ela.domain.usecase.reminder

import com.example.ela.domain.model.Reminder
import com.example.ela.domain.repository.ReminderRepository
import javax.inject.Inject

class SaveReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {

    suspend operator fun invoke(reminder: Reminder) {

        require(reminder.title.isNotBlank()) {
            "Título obrigatório"
        }

        repository.saveReminder(reminder)
    }
}