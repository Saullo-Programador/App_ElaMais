package com.example.ela.domain.usecase.important_date

import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.repository.ImportantDateRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportantDateUseCasesTest {

    private val repository = mockk<ImportantDateRepository>()
    private val date = ImportantDate(id = 1, title = "Aniversário", date = 1_000L, isRecurring = true)

    @Test
    fun `GetImportantDates deve retornar as datas do repositorio`() = runTest {
        every { repository.getDates() } returns flowOf(listOf(date))

        assertEquals(listOf(date), GetImportantDatesUseCase(repository)().first())
    }

    @Test
    fun `GetImportantDates deve retornar lista vazia quando nao ha datas`() = runTest {
        every { repository.getDates() } returns flowOf(emptyList())

        assertTrue(GetImportantDatesUseCase(repository)().first().isEmpty())
    }

    @Test
    fun `SaveImportantDate deve salvar a data`() = runTest {
        coEvery { repository.saveDate(date) } returns Unit

        SaveImportantDateUseCase(repository)(date)

        coVerify(exactly = 1) { repository.saveDate(date) }
    }

    @Test
    fun `DeleteImportantDate deve remover a data`() = runTest {
        coEvery { repository.deleteDate(date) } returns Unit

        DeleteImportantDateUseCase(repository)(date)

        coVerify(exactly = 1) { repository.deleteDate(date) }
    }

    @Test
    fun `SyncImportantDates deve sincronizar com a nuvem`() = runTest {
        coEvery { repository.syncDates() } returns Unit

        SyncImportantDatesUseCase(repository)()

        coVerify(exactly = 1) { repository.syncDates() }
    }
}
