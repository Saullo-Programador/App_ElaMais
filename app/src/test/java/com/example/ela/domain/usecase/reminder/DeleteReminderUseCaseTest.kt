package com.example.ela.domain.usecase.reminder

import com.example.ela.domain.model.Reminder
import com.example.ela.domain.repository.ReminderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class DeleteReminderUseCaseTest {

    private val repository: ReminderRepository = mockk()
    private val useCase = DeleteReminderUseCase(repository)

    @Test
    fun `should delete reminder via repository`() = runBlocking {
        val reminder = Reminder(1, "Title", "Desc", 12345L, "Type")
        coEvery { repository.deleteReminder(any()) } returns Unit

        useCase(reminder)

        coVerify { repository.deleteReminder(reminder) }
    }
}
