package com.example.ela.domain.usecase.cycle

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.CycleRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GetCycleCalendarDatesUseCaseTest {

    private val useCase = GetCycleCalendarDatesUseCase()

    /** Meio-dia da data informada em milissegundos (evita problemas de virada de dia/fuso). */
    private fun millisOf(date: LocalDate): Long =
        date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun range(start: LocalDate, fromOffset: Int, count: Int): List<LocalDate> =
        (fromOffset until fromOffset + count).map { start.plusDays(it.toLong()) }

    // ------------------------------------------------------------------
    // Sem dados
    // ------------------------------------------------------------------

    @Test
    fun `sem ciclo e sem historico deve retornar todas as listas vazias`() {
        val result = useCase(null, emptyList())

        assertTrue(result.menstruationDays.isEmpty())
        assertTrue(result.fertileDays.isEmpty())
        assertTrue(result.pmsDays.isEmpty())
        assertTrue(result.predictedDays.isEmpty())
    }

    @Test
    fun `com apenas um registro e sem ciclo deve retornar listas vazias`() {
        val history = listOf(CycleRecord(1, millisOf(LocalDate.now().minusDays(3)), 0))

        val result = useCase(null, history)

        assertTrue(result.menstruationDays.isEmpty())
    }

    // ------------------------------------------------------------------
    // Modo simples (ciclo 28 dias, menstruação 5 dias)
    // ------------------------------------------------------------------

    private val simpleStart: LocalDate = LocalDate.now().minusDays(10)

    private fun simpleResult() = useCase(
        Cycle(1, cycleLength = 28, periodLength = 5, lastPeriodStart = millisOf(simpleStart)),
        emptyList()
    )

    @Test
    fun `simples - dias de menstruacao do ciclo atual e do proximo`() {
        val expected = range(simpleStart, 0, 5) + range(simpleStart, 28, 5)

        assertEquals(expected, simpleResult().menstruationDays)
    }

    @Test
    fun `simples - janela fertil do ciclo atual e do proximo`() {
        // ovulação no dia 14 (ciclo / 2): janela do dia 11 ao 16
        val expected = range(simpleStart, 11, 6) + range(simpleStart, 39, 6)

        assertEquals(expected, simpleResult().fertileDays)
    }

    @Test
    fun `simples - TPM sao os 5 dias antes do fim de cada ciclo`() {
        val expected = range(simpleStart, 23, 5) + range(simpleStart, 51, 5)

        assertEquals(expected, simpleResult().pmsDays)
    }

    @Test
    fun `simples - previsao da proxima menstruacao comeca no fim do ciclo`() {
        val expected = range(simpleStart, 28, 5)

        assertEquals(expected, simpleResult().predictedDays)
    }

    @Test
    fun `simples - as listas ficam ordenadas e sem repeticao`() {
        val result = simpleResult()

        listOf(result.menstruationDays, result.fertileDays, result.pmsDays, result.predictedDays)
            .forEach { days ->
                assertEquals(days.sorted(), days)
                assertEquals(days.distinct(), days)
            }
    }

    @Test
    fun `simples - respeita a duracao da menstruacao configurada`() {
        val result = useCase(
            Cycle(1, cycleLength = 28, periodLength = 3, lastPeriodStart = millisOf(simpleStart)),
            emptyList()
        )

        assertEquals(range(simpleStart, 0, 3) + range(simpleStart, 28, 3), result.menstruationDays)
        assertEquals(range(simpleStart, 28, 3), result.predictedDays)
    }

    @Test
    fun `simples - com um unico registro no historico ainda usa o ciclo configurado`() {
        val history = listOf(CycleRecord(1, millisOf(simpleStart.plusDays(3)), 0))

        val result = useCase(
            Cycle(1, 28, 5, millisOf(simpleStart)),
            history
        )

        assertEquals(simpleStart, result.menstruationDays.first())
        assertEquals(10, result.menstruationDays.size)
    }

    // ------------------------------------------------------------------
    // Modo avançado (2+ registros no histórico)
    // ------------------------------------------------------------------

    private val firstStart: LocalDate = LocalDate.now().minusDays(45)
    private val lastStart: LocalDate = firstStart.plusDays(28)

    private fun advancedResult() = useCase(
        null,
        listOf(
            CycleRecord(1, millisOf(firstStart), millisOf(firstStart.plusDays(4))),
            CycleRecord(2, millisOf(lastStart), 0)
        )
    )

    @Test
    fun `avancado - marca como menstruacao os dias reais de cada registro`() {
        val result = advancedResult()

        // 1º registro: do início até o fim informado (inclusive)
        assertTrue(result.menstruationDays.containsAll(range(firstStart, 0, 5)))
        // último registro: sem data de fim, usa a duração padrão de 5 dias
        assertTrue(result.menstruationDays.containsAll(range(lastStart, 0, 5)))
    }

    @Test
    fun `avancado - inclui a menstruacao prevista do proximo ciclo`() {
        val result = advancedResult()

        assertTrue(result.menstruationDays.containsAll(range(lastStart, 28, 5)))
    }

    @Test
    fun `avancado - janela fertil calculada a partir do ultimo inicio`() {
        val result = advancedResult()

        // média 28 -> ovulação no dia 14, janela do dia 11 ao 16
        assertTrue(result.fertileDays.containsAll(range(lastStart, 11, 6)))
    }

    @Test
    fun `avancado - TPM nos 5 dias antes da proxima menstruacao`() {
        val result = advancedResult()

        assertTrue(result.pmsDays.containsAll(range(lastStart, 23, 5)))
    }

    @Test
    fun `avancado - previsao comeca depois de um ciclo completo`() {
        val result = advancedResult()

        assertEquals(range(lastStart, 28, 5), result.predictedDays)
    }

    @Test
    fun `avancado - nao mistura datas previstas com dias fertis`() {
        val result = advancedResult()

        assertFalse(result.predictedDays.any { it in result.fertileDays })
    }

    @Test
    fun `avancado - listas ordenadas e sem repeticao mesmo com registros fora de ordem`() {
        val result = useCase(
            null,
            listOf(
                CycleRecord(2, millisOf(lastStart), 0),
                CycleRecord(1, millisOf(firstStart), millisOf(firstStart.plusDays(4)))
            )
        )

        listOf(result.menstruationDays, result.fertileDays, result.pmsDays, result.predictedDays)
            .forEach { days ->
                assertEquals(days.sorted(), days)
                assertEquals(days.distinct(), days)
            }
        assertEquals(range(lastStart, 28, 5), result.predictedDays)
    }
}
