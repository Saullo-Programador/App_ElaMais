package com.example.ela.domain.usecase.cycle

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.repository.CycleRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class SaveCycleUseCaseTest {

    private val repository = mockk<CycleRepository>(relaxed = true)
    private val useCase = SaveCycleUseCase(repository)

    private fun cycle(cycleLength: Int = 28, periodLength: Int = 5) =
        Cycle(id = 0, cycleLength = cycleLength, periodLength = periodLength, lastPeriodStart = 1000L)

    // ------------------------------------------------------------------
    // Testes originais
    // ------------------------------------------------------------------

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

        assertFailsWith<IllegalArgumentException> {
            useCase(cycle)
        }
    }

    // ------------------------------------------------------------------
    // Limites: ciclo entre 20 e 40 dias, menstruação entre 2 e 10 dias
    // ------------------------------------------------------------------

    @Test
    fun `deve aceitar ciclo de 20 dias`() = runTest {
        useCase(cycle(cycleLength = 20))

        coVerify(exactly = 1) { repository.saveCycle(any()) }
    }

    @Test
    fun `deve aceitar ciclo de 40 dias`() = runTest {
        useCase(cycle(cycleLength = 40))

        coVerify(exactly = 1) { repository.saveCycle(any()) }
    }

    @Test
    fun `deve rejeitar ciclo de 19 dias`() = runTest {
        assertFailsWith<IllegalArgumentException> { useCase(cycle(cycleLength = 19)) }
    }

    @Test
    fun `deve rejeitar ciclo de 41 dias`() = runTest {
        assertFailsWith<IllegalArgumentException> { useCase(cycle(cycleLength = 41)) }
    }

    @Test
    fun `deve aceitar menstruacao de 2 dias`() = runTest {
        useCase(cycle(periodLength = 2))

        coVerify(exactly = 1) { repository.saveCycle(any()) }
    }

    @Test
    fun `deve aceitar menstruacao de 10 dias`() = runTest {
        useCase(cycle(periodLength = 10))

        coVerify(exactly = 1) { repository.saveCycle(any()) }
    }

    @Test
    fun `deve rejeitar menstruacao de 1 dia`() = runTest {
        assertFailsWith<IllegalArgumentException> { useCase(cycle(periodLength = 1)) }
    }

    @Test
    fun `deve rejeitar menstruacao de 11 dias`() = runTest {
        assertFailsWith<IllegalArgumentException> { useCase(cycle(periodLength = 11)) }
    }

    @Test
    fun `deve rejeitar valores zero ou negativos`() = runTest {
        assertFailsWith<IllegalArgumentException> { useCase(cycle(cycleLength = 0)) }
        assertFailsWith<IllegalArgumentException> { useCase(cycle(cycleLength = -28)) }
        assertFailsWith<IllegalArgumentException> { useCase(cycle(periodLength = 0)) }
        assertFailsWith<IllegalArgumentException> { useCase(cycle(periodLength = -5)) }
    }

    // ------------------------------------------------------------------
    // Mensagens e efeitos colaterais
    // ------------------------------------------------------------------

    @Test
    fun `ciclo invalido deve informar a mensagem de ciclo invalido`() = runTest {
        val error = assertFailsWith<IllegalArgumentException> { useCase(cycle(cycleLength = 10)) }

        assertEquals("Ciclo inválido", error.message)
    }

    @Test
    fun `menstruacao invalida deve informar a mensagem de duracao invalida`() = runTest {
        val error = assertFailsWith<IllegalArgumentException> { useCase(cycle(periodLength = 15)) }

        assertEquals("Duração da menstruação inválida", error.message)
    }

    @Test
    fun `nao deve chamar o repositorio quando os dados sao invalidos`() = runTest {
        assertFailsWith<IllegalArgumentException> { useCase(cycle(cycleLength = 10)) }
        assertFailsWith<IllegalArgumentException> { useCase(cycle(periodLength = 15)) }

        coVerify(exactly = 0) { repository.saveCycle(any()) }
    }
}
