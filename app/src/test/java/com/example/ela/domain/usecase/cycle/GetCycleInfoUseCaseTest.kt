package com.example.ela.domain.usecase.cycle

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.model.CycleRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class GetCycleInfoUseCaseTest {

    private val useCase = GetCycleInfoUseCase()

    @Test
    fun `test calculateSimple returns MENSTRUAL phase when on day 3`() {
        // Preparação: menstruação começou há 3 dias
        val threeDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)
        val cycle = Cycle(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = threeDaysAgo
        )

        // Ação
        val result = useCase(cycle, emptyList())

        // Verificação
        assertEquals("Deveria estar na fase MENSTRUAL no 3º dia", CyclePhase.MENSTRUAL, result.currentPhase)
        assertTrue("Deveria indicar que possui dados", result.hasData)
        // Ciclo total = 28 + 5 = 33. Dia atual = 3. Faltam 33 - 3 = 30 dias.
        assertEquals("Faltam 30 dias para o próximo ciclo", 30, result.daysUntilNextPeriod)
    }

    @Test
    fun `test calculateWithHistory calculates average based on last cycles`() {
        // Preparação: 2 ciclos completos (um de 28 e outro de 30 dias). Média = 29.
        // Hoje é o início de um novo ciclo (dia 0)
        val today = System.currentTimeMillis()
        val startCycle3 = today
        val startCycle2 = startCycle3 - TimeUnit.DAYS.toMillis(30)
        val startCycle1 = startCycle2 - TimeUnit.DAYS.toMillis(28)

        val history = listOf(
            CycleRecord(1, startCycle1, startCycle1 + TimeUnit.DAYS.toMillis(5)),
            CycleRecord(2, startCycle2, startCycle2 + TimeUnit.DAYS.toMillis(5)),
            CycleRecord(3, startCycle3, 0) // Ciclo atual iniciado hoje
        )

        // Ação
        val result = useCase(null, history)

        // Verificação
        // Média de ciclo (intervalo entre inícios) = (28 + 30) / 2 = 29.
        // Adicionando a duração da menstruação (5 dias): Total = 29 + 5 = 34.
        // Dia atual = 0.
        // Dias até o próximo = 34 - 0 = 34.
        assertEquals("A média de ciclo total deveria ser 34 dias", 34, result.daysUntilNextPeriod)
        assertEquals("No dia 0 a fase deve ser MENSTRUAL", CyclePhase.MENSTRUAL, result.currentPhase)
    }

    @Test
    fun `test calculateSimple returns TPM phase near end of cycle`() {
        // Preparação: ciclo de 28 dias, menstruação de 5 dias. Ciclo Total = 33.
        // Começou há 25 dias (está no dia 25).
        // TPM começa em Total - 5 (33 - 5 = 28).
        // No dia 25, ainda não é TPM. Vamos testar no dia 29.
        val twentyNineDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(29)
        val cycle = Cycle(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = twentyNineDaysAgo
        )

        // Ação
        val result = useCase(cycle, emptyList())

        // Verificação
        assertEquals("Deveria estar na fase TPM no dia 29", CyclePhase.TPM, result.currentPhase)
        assertTrue("Deveria indicar estado de TPM", result.isPms)
    }
}
