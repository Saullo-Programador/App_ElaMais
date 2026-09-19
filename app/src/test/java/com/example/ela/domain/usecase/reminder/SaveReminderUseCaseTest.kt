package com.example.ela.domain.usecase.reminder

import com.example.ela.domain.model.Reminder
import com.example.ela.domain.repository.ReminderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SaveReminderUseCaseTest {

    private val repository: ReminderRepository = mockk()
    private val useCase = SaveReminderUseCase(repository)

    @Test
    fun `should save reminder when title is not blank`() = runBlocking {
        val reminder = Reminder(1, "Title", "Desc", 12345L, "Type")
        coEvery { repository.saveReminder(any()) } returns Unit

        useCase(reminder)

        coVerify { repository.saveReminder(reminder) }
    }

    @Test
    fun `should throw exception when title is blank`() {
        val reminder = Reminder(1, "", "Desc", 12345L, "Type")

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(reminder) }
        }
    }
}
