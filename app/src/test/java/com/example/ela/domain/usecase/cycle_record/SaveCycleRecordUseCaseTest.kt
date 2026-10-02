package com.example.ela.domain.usecase.cycle_record

import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.repository.CycleRecordRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class SaveCycleRecordUseCaseTest {

    private val repository = mockk<CycleRecordRepository>()
    private val useCase = SaveCycleRecordUseCase(repository)
    private val saved = slot<CycleRecord>()

    @Before
    fun setup() {
        coEvery { repository.save(capture(saved)) } returns Unit
    }

    @Test
    fun `deve salvar registro apenas com data de inicio`() = runTest {
        useCase(1_000L)

        assertEquals(CycleRecord(id = 0, startDate = 1_000L, endDate = 0L), saved.captured)
    }

    @Test
    fun `deve salvar registro com inicio e fim`() = runTest {
        useCase(1_000L, 5_000L)

        assertEquals(CycleRecord(id = 0, startDate = 1_000L, endDate = 5_000L), saved.captured)
    }

    @Test
    fun `deve aceitar fim igual ao inicio`() = runTest {
        useCase(1_000L, 1_000L)

        assertEquals(1_000L, saved.captured.endDate)
    }

    @Test
    fun `deve sempre criar o registro com id zero`() = runTest {
        useCase(1_000L, 2_000L)

        assertEquals(0L, saved.captured.id)
    }

    @Test
    fun `deve rejeitar fim anterior ao inicio`() = runTest {
        val error = assertFailsWith<IllegalArgumentException> {
            useCase(5_000L, 1_000L)
        }

        assertEquals("Data inválida", error.message)
    }

    @Test
    fun `nao deve salvar quando a data de fim e invalida`() = runTest {
        assertFailsWith<IllegalArgumentException> { useCase(5_000L, 1_000L) }

        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `fim igual a zero significa menstruacao em andamento e nao deve validar`() = runTest {
        // Início muito recente e fim = 0: não pode lançar exceção
        useCase(9_999_999L, 0L)

        coVerify(exactly = 1) { repository.save(any()) }
    }
}
