package com.example.ela.domain.usecase.cycle

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.repository.CycleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.test.assertFailsWith

class SaveCycleUseCaseTest {

    private val repository = mockk<CycleRepository>(relaxed = true)
    private val useCase = SaveCycleUseCase(repository)

    @Test
    fun `when cycle is valid, it should be saved`() = runTest {
        val cycle = Cycle(id = 0, cycleLength = 28, periodLength = 5, lastPeriodStart = 1000L)
        
        useCase(cycle)
        
        coVerify { repository.saveCycle(cycle) }
    }

    @Test
    fun `when cycle length is too short, it should throw exception`() = runTest {
        val cycle = Cycle(id = 0, cycleLength = 10, periodLength = 5, lastPeriodStart = 1000L)

        assertFailsWith<IllegalArgumentException> {
            useCase(cycle)
        }
    }

    @Test
    fun `when period length is too long, it should throw exception`() = runTest {
        val cycle = Cycle(id = 0, cycleLength = 28, periodLength = 15, lastPeriodStart = 1000L)
        
        assertFailsWith <IllegalArgumentException> {
            useCase(cycle)
        }
    }
}
