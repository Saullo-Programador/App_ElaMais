package com.example.ela.domain.usecase.reminder

import com.example.ela.domain.model.Reminder
import com.example.ela.domain.repository.ReminderRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRemindersUseCaseTest {

    private val repository: ReminderRepository = mockk()
    private val useCase = GetRemindersUseCase(repository)

    @Test
    fun `should return reminders from repository`() = runTest {
        val reminders = listOf(
            Reminder(1, "T1", "D1", 123L, "Type1"),
            Reminder(2, "T2", "D2", 456L, "Type2")
        )
        every { repository.getReminder() } returns flowOf(reminders)

        val result = useCase()

        assertEquals(reminders, result.first())
    }
}
