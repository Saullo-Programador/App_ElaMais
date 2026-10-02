package com.example.ela.domain.usecase.cycle

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.repository.CycleRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetCycleUseCaseTest {

    private val repository = mockk<CycleRepository>()
    private val useCase = GetCycleUseCase(repository)

    @Test
    fun `deve retornar o ciclo do repositorio`() = runTest {
        val cycle = Cycle(1, 28, 5, 1000L)
        every { repository.getCycle() } returns flowOf(cycle)

        assertEquals(cycle, useCase().first())
    }

    @Test
    fun `deve retornar null quando nao ha ciclo salvo`() = runTest {
        every { repository.getCycle() } returns flowOf(null)

        assertNull(useCase().first())
    }
}
