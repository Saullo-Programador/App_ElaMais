package com.example.ela.domain.usecase.cycle

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.CycleInfo
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.model.CycleRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class GetCycleInfoUseCaseTest {

    private val useCase = GetCycleInfoUseCase()

    /**
     * Momento "há N dias".
     * Uma hora de folga garante que a divisão em dias não mude
     * caso o teste demore alguns milissegundos para rodar.
     */
    private fun daysAgo(days: Int): Long =
        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong()) - TimeUnit.HOURS.toMillis(1)

    private fun days(n: Int): Long = TimeUnit.DAYS.toMillis(n.toLong())

    /** Ciclo de 28 dias + 5 de menstruação (total = 33), começando há [daysIntoCycle] dias. */
    private fun simpleInfo(daysIntoCycle: Int): CycleInfo =
        useCase(Cycle(1, cycleLength = 28, periodLength = 5, lastPeriodStart = daysAgo(daysIntoCycle)), emptyList())

    /** Histórico regular de 28 dias, com o ciclo atual começando há [daysIntoCycle] dias. */
    private fun historyInfo(daysIntoCycle: Int): CycleInfo {
        val current = daysAgo(daysIntoCycle)
        val previous = current - days(28)
        val first = previous - days(28)
        return useCase(
            null,
            listOf(
                CycleRecord(1, first, first + days(5)),
                CycleRecord(2, previous, previous + days(5)),
                CycleRecord(3, current, 0)
            )
        )
    }

    // ------------------------------------------------------------------
    // Testes originais
    // ------------------------------------------------------------------

    @Test
    fun `test calculateSimple returns MENSTRUAL phase when on day 3`() {
        val threeDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)
        val cycle = Cycle(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = threeDaysAgo
        )

        val result = useCase(cycle, emptyList())

        assertEquals("Deveria estar na fase MENSTRUAL no 3º dia", CyclePhase.MENSTRUAL, result.currentPhase)
        assertTrue("Deveria indicar que possui dados", result.hasData)
        // Ciclo total = 28 + 5 = 33. Dia atual = 3. Faltam 33 - 3 = 30 dias.
        assertEquals("Faltam 30 dias para o próximo ciclo", 30, result.daysUntilNextPeriod)
    }

    @Test
    fun `test calculateWithHistory calculates average based on last cycles`() {
        val today = System.currentTimeMillis()
        val startCycle3 = today
        val startCycle2 = startCycle3 - TimeUnit.DAYS.toMillis(30)
        val startCycle1 = startCycle2 - TimeUnit.DAYS.toMillis(28)

        val history = listOf(
            CycleRecord(1, startCycle1, startCycle1 + TimeUnit.DAYS.toMillis(5)),
            CycleRecord(2, startCycle2, startCycle2 + TimeUnit.DAYS.toMillis(5)),
            CycleRecord(3, startCycle3, 0)
        )

        val result = useCase(null, history)

        // Média entre inícios = (28 + 30) / 2 = 29. Total = 29 + 5 = 34. Dia atual = 0.
        assertEquals("A média de ciclo total deveria ser 34 dias", 34, result.daysUntilNextPeriod)
        assertEquals("No dia 0 a fase deve ser MENSTRUAL", CyclePhase.MENSTRUAL, result.currentPhase)
    }

    @Test
    fun `test calculateSimple returns TPM phase near end of cycle`() {
        val twentyNineDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(29)
        val cycle = Cycle(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = twentyNineDaysAgo
        )

        val result = useCase(cycle, emptyList())

        assertEquals("Deveria estar na fase TPM no dia 29", CyclePhase.TPM, result.currentPhase)
        assertTrue("Deveria indicar estado de TPM", result.isPms)
    }

    // ------------------------------------------------------------------
    // Sem dados
    // ------------------------------------------------------------------

    @Test
    fun `sem ciclo e sem historico deve retornar CycleInfo vazio`() {
        val result = useCase(null, emptyList())

        assertEquals(CycleInfo.empty(), result)
        assertFalse(result.hasData)
    }

    @Test
    fun `com apenas um registro no historico e sem ciclo deve retornar vazio`() {
        val history = listOf(CycleRecord(1, daysAgo(2), 0))

        val result = useCase(null, history)

        assertFalse(result.hasData)
    }

    @Test
    fun `com apenas um registro no historico deve usar os dados do ciclo configurado`() {
        val cycle = Cycle(1, 28, 5, daysAgo(2))
        val history = listOf(CycleRecord(1, daysAgo(40), 0))

        val result = useCase(cycle, history)

        assertTrue(result.hasData)
        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(31, result.daysUntilNextPeriod)
    }

    // ------------------------------------------------------------------
    // Modo simples: fases
    // ------------------------------------------------------------------

    @Test
    fun `simples - no dia 0 esta menstruada com 5 dias restantes`() {
        val result = simpleInfo(0)

        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(33, result.daysUntilNextPeriod)
        assertEquals(5, result.daysRemainingInPhase)
    }

    @Test
    fun `simples - ultimo dia da menstruacao ainda e MENSTRUAL com 1 dia restante`() {
        val result = simpleInfo(4)

        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(1, result.daysRemainingInPhase)
    }

    @Test
    fun `simples - dia seguinte ao fim da menstruacao vira FOLLICULAR`() {
        val result = simpleInfo(5)

        assertEquals(CyclePhase.FOLLICULAR, result.currentPhase)
        assertEquals(0, result.daysRemainingInPhase)
    }

    @Test
    fun `simples - dia 10 esta na fase folicular`() {
        val result = simpleInfo(10)

        assertEquals(CyclePhase.FOLLICULAR, result.currentPhase)
        assertEquals(23, result.daysUntilNextPeriod)
    }

    @Test
    fun `simples - dias 19 a 21 estao na ovulacao`() {
        listOf(19, 20, 21).forEach { day ->
            assertEquals("Dia $day", CyclePhase.OVULATION, simpleInfo(day).currentPhase)
        }
    }

    @Test
    fun `simples - dia 22 ja e fase lutea`() {
        assertEquals(CyclePhase.LUTEAL, simpleInfo(22).currentPhase)
    }

    @Test
    fun `simples - dia 27 ainda e lutea e sem TPM`() {
        val result = simpleInfo(27)

        assertEquals(CyclePhase.LUTEAL, result.currentPhase)
        assertFalse(result.isPms)
    }

    @Test
    fun `simples - dia 28 comeca a TPM`() {
        val result = simpleInfo(28)

        assertEquals(CyclePhase.TPM, result.currentPhase)
        assertTrue(result.isPms)
        assertEquals(5, result.daysUntilNextPeriod)
    }

    @Test
    fun `simples - ultimo dia do ciclo ainda e TPM e falta 1 dia`() {
        val result = simpleInfo(32)

        assertEquals(CyclePhase.TPM, result.currentPhase)
        assertEquals(1, result.daysUntilNextPeriod)
    }

    // ------------------------------------------------------------------
    // Modo simples: ciclo que se repete e datas inesperadas
    // ------------------------------------------------------------------

    @Test
    fun `simples - depois do fim do ciclo comeca um novo ciclo`() {
        val result = simpleInfo(33)

        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(33, result.daysUntilNextPeriod)
    }

    @Test
    fun `simples - ciclo muito atrasado continua repetindo corretamente`() {
        // 66 dias = exatamente 2 ciclos completos de 33 dias
        val result = simpleInfo(66)

        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(33, result.daysUntilNextPeriod)
    }

    @Test
    fun `simples - data de inicio no futuro e tratada como dia 0`() {
        val cycle = Cycle(1, 28, 5, System.currentTimeMillis() + days(2))

        val result = useCase(cycle, emptyList())

        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(33, result.daysUntilNextPeriod)
    }

    @Test
    fun `simples - usa a soma de ciclo e menstruacao configurados`() {
        // 30 + 7 = 37 dias no total, hoje é o dia 3
        val cycle = Cycle(1, cycleLength = 30, periodLength = 7, lastPeriodStart = daysAgo(3))

        val result = useCase(cycle, emptyList())

        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(34, result.daysUntilNextPeriod)
        assertEquals(4, result.daysRemainingInPhase)
    }

    // ------------------------------------------------------------------
    // Modo simples: janela fértil
    // ------------------------------------------------------------------

    @Test
    fun `simples - um dia antes da janela fertil ainda nao e fertil`() {
        val result = simpleInfo(15)

        assertFalse(result.isFertileWindow)
        assertEquals(1, result.daysUntilFertileWindow)
    }

    @Test
    fun `simples - primeiro dia da janela fertil`() {
        val result = simpleInfo(16)

        assertTrue(result.isFertileWindow)
        assertEquals(0, result.daysUntilFertileWindow)
    }

    @Test
    fun `simples - ultimo dia da janela fertil`() {
        assertTrue(simpleInfo(21).isFertileWindow)
    }

    @Test
    fun `simples - depois da janela fertil nao e mais fertil`() {
        assertFalse(simpleInfo(22).isFertileWindow)
    }

    @Test
    fun `simples - dias ate a TPM diminuem ao longo do ciclo`() {
        assertEquals(28, simpleInfo(0).daysUntilPms)
        assertEquals(18, simpleInfo(10).daysUntilPms)
        assertEquals(1, simpleInfo(27).daysUntilPms)
        assertEquals(0, simpleInfo(28).daysUntilPms)
    }

    // ------------------------------------------------------------------
    // Sugestões
    // ------------------------------------------------------------------

    @Test
    fun `sugestoes - TPM tem 3 sugestoes`() {
        val result = simpleInfo(29)

        assertEquals(3, result.suggestions.size)
        assertTrue(result.suggestions.first().startsWith("Comprar chocolate"))
    }

    @Test
    fun `sugestoes - menstrual tem 2 sugestoes`() {
        assertEquals(2, simpleInfo(1).suggestions.size)
    }

    @Test
    fun `sugestoes - ovulacao tem 1 sugestao de encontro`() {
        val result = simpleInfo(20)

        assertEquals(1, result.suggestions.size)
        assertTrue(result.suggestions.first().startsWith("Planejar um encontro"))
    }

    @Test
    fun `sugestoes - fases folicular e lutea usam a sugestao padrao`() {
        val folicular = simpleInfo(10).suggestions
        val lutea = simpleInfo(24).suggestions

        assertEquals(1, folicular.size)
        assertEquals(folicular, lutea)
    }

    // ------------------------------------------------------------------
    // Modo com histórico
    // ------------------------------------------------------------------

    @Test
    fun `historico - dia 0 esta menstruada e faltam 33 dias`() {
        val result = historyInfo(0)

        assertTrue(result.hasData)
        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(33, result.daysUntilNextPeriod)
        assertEquals(5, result.daysRemainingInPhase)
    }

    @Test
    fun `historico - ultimo dia da menstruacao`() {
        val result = historyInfo(4)

        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(1, result.daysRemainingInPhase)
    }

    @Test
    fun `historico - dia 10 esta na fase folicular`() {
        val result = historyInfo(10)

        assertEquals(CyclePhase.FOLLICULAR, result.currentPhase)
        assertFalse(result.isFertileWindow)
        assertEquals(6, result.daysUntilFertileWindow)
    }

    @Test
    fun `historico - dia 19 e ovulacao dentro da janela fertil`() {
        val result = historyInfo(19)

        assertEquals(CyclePhase.OVULATION, result.currentPhase)
        assertTrue(result.isFertileWindow)
    }

    @Test
    fun `historico - dia 24 esta na fase lutea`() {
        val result = historyInfo(24)

        assertEquals(CyclePhase.LUTEAL, result.currentPhase)
        assertFalse(result.isFertileWindow)
    }

    @Test
    fun `historico - dia 28 indica TPM`() {
        val result = historyInfo(28)

        assertEquals(CyclePhase.TPM, result.currentPhase)
        assertTrue(result.isPms)
    }

    @Test
    fun `historico - depois do fim do ciclo comeca um novo`() {
        val result = historyInfo(33)

        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(33, result.daysUntilNextPeriod)
    }

    @Test
    fun `historico - com apenas dois registros usa o unico intervalo existente`() {
        val current = daysAgo(0)
        val previous = current - days(30)
        val history = listOf(
            CycleRecord(1, previous, previous + days(5)),
            CycleRecord(2, current, 0)
        )

        val result = useCase(null, history)

        // intervalo = 30, menstruação padrão = 5, total = 35
        assertEquals(35, result.daysUntilNextPeriod)
    }

    @Test
    fun `historico - usa a duracao real da menstruacao do ultimo registro`() {
        val current = daysAgo(0)
        val previous = current - days(28)
        val first = previous - days(28)
        val history = listOf(
            CycleRecord(1, first, first),
            CycleRecord(2, previous, previous),
            CycleRecord(3, current, current + days(7))
        )

        val result = useCase(null, history)

        // média 28 + menstruação de 7 dias = 35
        assertEquals(35, result.daysUntilNextPeriod)
        assertEquals(7, result.daysRemainingInPhase)
    }

    @Test
    fun `historico - ordena os registros mesmo que venham fora de ordem`() {
        val current = daysAgo(0)
        val second = current - days(30)
        val first = second - days(28)
        val shuffled = listOf(
            CycleRecord(3, current, 0),
            CycleRecord(1, first, first + days(5)),
            CycleRecord(2, second, second + days(5))
        )

        val result = useCase(null, shuffled)

        // mesma conta do teste original: média 29 + 5 = 34
        assertEquals(34, result.daysUntilNextPeriod)
    }

    @Test
    fun `historico - ignora intervalos absurdos ao calcular a media`() {
        val current = daysAgo(0)
        val b = current - days(30)
        val c = b - days(60) // intervalo de 60 dias: fora de 20..40
        val d = c - days(26)
        val history = listOf(
            CycleRecord(1, d, 0),
            CycleRecord(2, c, 0),
            CycleRecord(3, b, 0),
            CycleRecord(4, current, 0)
        )

        val result = useCase(null, history)

        // intervalos: 26, 60 (ignorado), 30 -> média 28 + 5 = 33
        assertEquals(33, result.daysUntilNextPeriod)
    }

    @Test
    fun `historico - considera apenas os 6 ciclos mais recentes`() {
        // intervalos do registro mais novo para o mais antigo
        val intervals = listOf(22, 22, 22, 22, 22, 40, 40)
        val starts = mutableListOf(daysAgo(0))
        intervals.forEach { starts.add(starts.last() - days(it)) }

        val history = starts.mapIndexed { index, start -> CycleRecord(index + 1L, start, 0) }

        val result = useCase(null, history)

        // só os 6 mais recentes contam: 5 intervalos de 22 -> média 22 + 5 = 27
        // (se os 8 fossem usados, o resultado seria 32)
        assertEquals(27, result.daysUntilNextPeriod)
    }
}
