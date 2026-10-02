package com.example.ela.domain.usecase.settings

import com.example.ela.domain.repository.CareActionRepository
import com.example.ela.domain.repository.CycleRecordRepository
import com.example.ela.domain.repository.CycleRepository
import com.example.ela.domain.repository.ImportantDateRepository
import com.example.ela.domain.repository.PreferencesRepository
import com.example.ela.domain.repository.ReminderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class ClearAllDataUseCaseTest {

    private val cycleRepository = mockk<CycleRepository>(relaxed = true)
    private val careActionRepository = mockk<CareActionRepository>(relaxed = true)
    private val importantDateRepository = mockk<ImportantDateRepository>(relaxed = true)
    private val reminderRepository = mockk<ReminderRepository>(relaxed = true)
    private val cycleRecordRepository = mockk<CycleRecordRepository>(relaxed = true)
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)

    private val useCase = ClearAllDataUseCase(
        cycleRepository,
        careActionRepository,
        importantDateRepository,
        reminderRepository,
        cycleRecordRepository,
        preferencesRepository
    )

    @Test
    fun `deve limpar os dados de todos os repositorios`() = runTest {
        useCase()

        coVerify(exactly = 1) { cycleRepository.deleteAll() }
        coVerify(exactly = 1) { careActionRepository.deleteAll() }
        coVerify(exactly = 1) { importantDateRepository.deleteAll() }
        coVerify(exactly = 1) { reminderRepository.deleteAll() }
        coVerify(exactly = 1) { cycleRecordRepository.deleteAll() }
        coVerify(exactly = 1) { preferencesRepository.deleteAll() }
    }

    @Test
    fun `deve propagar a falha de um repositorio para quem chamou`() = runTest {
        coEvery { reminderRepository.deleteAll() } throws RuntimeException("falha ao limpar lembretes")

        val error = assertFailsWith<RuntimeException> { useCase() }

        assertEquals("falha ao limpar lembretes", error.message)
    }

    @Test
    fun `deve poder ser executado mais de uma vez`() = runTest {
        useCase()
        useCase()

        coVerify(exactly = 2) { cycleRepository.deleteAll() }
        coVerify(exactly = 2) { preferencesRepository.deleteAll() }
    }
}
