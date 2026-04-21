package com.example.ela.domain.usecase.cycle

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.CycleInfo
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.model.CycleRecord
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

/**
 * UseCase responsável por calcular informações do ciclo menstrual.
 *
 * Ele funciona em dois modos:
 *
 * 🔹 Modo simples (MVP)
 * → quando há pouco histórico (menos de 2 ciclos)
 *
 * 🔥 Modo avançado (nível Flo)
 * → quando há histórico suficiente (2+ ciclos)
 */
class GetCycleInfoUseCase @Inject constructor() {

    operator fun invoke(
        cycle: Cycle?,
        history: List<CycleRecord>
    ): CycleInfo {

        return if (history.size >= 2) {
            calculateWithHistory(history)
        } else {
            // fallback seguro (NUNCA quebrar o app)
            cycle?.let { calculateSimple(it) } ?: CycleInfo.Companion.empty()
        }
    }

    /**
     * 🔹 Cálculo simples baseado em dados fixos
     */
    private fun calculateSimple(cycle: Cycle): CycleInfo {

        val today = System.currentTimeMillis()
        val diff = today - cycle.lastPeriodStart

        val daysSinceStart = TimeUnit.MILLISECONDS.toDays(diff).toInt()
        val currentDay = (daysSinceStart % cycle.cycleLength).coerceAtLeast(0)

        val phase = getPhaseSimple(currentDay, cycle)
        val daysUntilNext = (cycle.cycleLength - currentDay).coerceAtLeast(0)

        val isFertile = currentDay in getFertileRange(cycle)
        val isPms = currentDay >= (cycle.cycleLength - 5)

        return CycleInfo(
            currentPhase = phase,
            daysUntilNextPeriod = daysUntilNext,
            isFertileWindow = isFertile,
            isPms = isPms,
            suggestions = getSuggestions(phase),
            hasData = true
        )
    }

    /**
     * 🔹 Fase no modo simples
     */
    private fun getPhaseSimple(day: Int, cycle: Cycle): CyclePhase {
        return when {
            day < cycle.periodLength -> CyclePhase.MENSTRUAL
            day < 14 -> CyclePhase.FOLLICULAR
            day in 14..16 -> CyclePhase.OVULATION
            day >= (cycle.cycleLength - 5) -> CyclePhase.TPM
            else -> CyclePhase.LUTEAL
        }
    }

    /**
     * 🔥 Cálculo inteligente baseado em histórico real
     */
    private fun calculateWithHistory(history: List<CycleRecord>): CycleInfo {

        val sorted = history.sortedBy { it.startDate }

        // Usa apenas últimos 6 ciclos (mais preciso)
        val recent = sorted.takeLast(6)

        val cycleLengths = calculateCycleLengths(recent)

        val avgCycle = if (cycleLengths.isNotEmpty()) {
            cycleLengths
                .filter { it in 20..40 } // evita valores absurdos
                .average()
                .toInt()
                .coerceAtLeast(21)
        } else {
            28
        }

        val lastCycle = recent.last()
        val today = System.currentTimeMillis()

        val daysSinceStart = daysBetween(lastCycle.startDate, today)
        val currentDay = (daysSinceStart % avgCycle).coerceAtLeast(0)

        val ovulationDay = (avgCycle - 14).coerceAtLeast(1)
        val fertileRange = (ovulationDay - 3)..(ovulationDay + 2)

        val isFertile = currentDay in fertileRange
        val isPms = currentDay >= (avgCycle - 5)

        val phase = getPhaseAdvanced(currentDay, lastCycle, ovulationDay)

        return CycleInfo(
            currentPhase = phase,
            daysUntilNextPeriod = (avgCycle - currentDay).coerceAtLeast(0),
            isFertileWindow = isFertile,
            isPms = isPms,
            suggestions = getSuggestions(phase),
            hasData = true
        )
    }

    /**
     * 📊 Calcula duração dos ciclos
     */
    private fun calculateCycleLengths(cycles: List<CycleRecord>): List<Int> {
        val list = mutableListOf<Int>()

        for (i in 0 until cycles.size - 1) {
            val diff = daysBetween(
                cycles[i].startDate,
                cycles[i + 1].startDate
            )
            list.add(diff)
        }

        return list
    }

    /**
     * 🔥 Fase avançada baseada em histórico
     */
    private fun getPhaseAdvanced(
        day: Int,
        lastCycle: CycleRecord,
        ovulationDay: Int
    ): CyclePhase {

        val periodLength = if (lastCycle.endDate > 0) {
            daysBetween(lastCycle.startDate, lastCycle.endDate)
        } else {
            5
        }

        return when {
            day < periodLength -> CyclePhase.MENSTRUAL
            day < ovulationDay -> CyclePhase.FOLLICULAR
            day in (ovulationDay - 1..ovulationDay + 1) -> CyclePhase.OVULATION
            day >= (ovulationDay + 2) && day < (ovulationDay + 7) -> CyclePhase.LUTEAL
            else -> CyclePhase.TPM
        }
    }

    /**
     * 🌸 Período fértil (modo simples)
     */
    private fun getFertileRange(cycle: Cycle): IntRange {
        val ovulationDay = cycle.cycleLength / 2
        return (ovulationDay - 3)..(ovulationDay + 2)
    }

    /**
     * 📅 Diferença em dias
     */
    private fun daysBetween(start: Long, end: Long): Int {
        return abs((end - start) / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * 💡 Sugestões por fase
     */
    private fun getSuggestions(phase: CyclePhase): List<String> {
        return when (phase) {

            CyclePhase.TPM -> listOf(
                "Comprar chocolate 🍫",
                "Evitar discussões 😅",
                "Ser mais paciente ❤️"
            )

            CyclePhase.MENSTRUAL -> listOf(
                "Fazer massagem nas pernas 🦵",
                "Oferecer conforto 🛌"
            )

            CyclePhase.OVULATION -> listOf(
                "Planejar um encontro 💑"
            )

            else -> listOf("Manter rotina 🙂")
        }
    }
}