package com.example.ela.domain.usecase.reminder

import com.example.ela.domain.repository.ReminderRepository
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke() = repository.getReminder()
}