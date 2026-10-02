package com.example.ela.domain.usecase.cycle_record

import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.repository.CycleRecordRepository
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

class CycleRecordUseCasesTest {

    private val repository = mockk<CycleRecordRepository>()

    @Test
    fun `GetCycleHistory deve retornar o historico do repositorio`() = runTest {
        val records = listOf(CycleRecord(1, 1_000L, 2_000L), CycleRecord(2, 3_000L, 0L))
        every { repository.getHistory() } returns flowOf(records)

        val result = GetCycleHistoryUseCase(repository)().first()

        assertEquals(records, result)
    }

    @Test
    fun `GetCycleHistory deve retornar lista vazia quando nao ha historico`() = runTest {
        every { repository.getHistory() } returns flowOf(emptyList())

        val result = GetCycleHistoryUseCase(repository)().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `DeleteCycleRecord deve remover o registro pelo repositorio`() = runTest {
        val record = CycleRecord(7, 1_000L, 2_000L)
        coEvery { repository.delete(record) } returns Unit

        DeleteCycleRecordUseCase(repository)(record)

        coVerify(exactly = 1) { repository.delete(record) }
    }
}
