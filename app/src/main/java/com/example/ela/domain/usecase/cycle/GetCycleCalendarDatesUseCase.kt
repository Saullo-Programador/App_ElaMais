package com.example.ela.domain.usecase.cycle

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.CycleCalendarDates
import com.example.ela.domain.model.CycleRecord
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
class GetCycleCalendarDatesUseCase @Inject constructor() {

    operator fun invoke(
        cycle: Cycle?,
        history: List<CycleRecord>
    ): CycleCalendarDates {
        val today = LocalDate.now()
        val nowMillis = System.currentTimeMillis()

        return if (history.size >= 2) {
            calculateAdvanced(history, today, nowMillis)
        } else {
            cycle?.let { calculateSimple(it, today, nowMillis) } ?: emptyDates()
        }
    }

    private fun calculateSimple(cycle: Cycle, today: LocalDate, nowMillis: Long): CycleCalendarDates {
        val lastPeriodStart = Instant.ofEpochMilli(cycle.lastPeriodStart)
            .atZone(ZoneId.systemDefault()).toLocalDate()

        val daysSinceStart = TimeUnit.MILLISECONDS.toDays(nowMillis - cycle.lastPeriodStart).toInt().coerceAtLeast(0)
        val currentDay = (daysSinceStart % cycle.cycleLength).coerceAtLeast(0)

        val ovulationDay = cycle.cycleLength / 2
        val fertileRange = (ovulationDay - 3)..(ovulationDay + 2)

        return buildDates(
            lastPeriodStart = lastPeriodStart,
            cycleLength = cycle.cycleLength,
            periodLength = cycle.periodLength,
            ovulationDay = ovulationDay,
            fertileRange = fertileRange,
            history = emptyList(),
            today = today
        )
    }

    private fun calculateAdvanced(history: List<CycleRecord>, today: LocalDate, nowMillis: Long): CycleCalendarDates {
        val sorted = history.sortedBy { it.startDate }
        val recent = sorted.takeLast(6)

        val cycleLengths = mutableListOf<Int>()
        for (i in 0 until recent.size - 1) {
            cycleLengths.add(abs((recent[i+1].startDate - recent[i].startDate) / (1000 * 60 * 60 * 24)).toInt())
        }

        val avgCycle = if (cycleLengths.isNotEmpty()) {
            cycleLengths.filter { it in 20..40 }.average().toInt().coerceAtLeast(21)
        } else {
            28
        }

        val lastCycle = recent.last()
        val lastPeriodStart = Instant.ofEpochMilli(lastCycle.startDate)
            .atZone(ZoneId.systemDefault()).toLocalDate()

        val periodLength = if (lastCycle.endDate > 0) {
            abs((lastCycle.endDate - lastCycle.startDate) / (1000 * 60 * 60 * 24)).toInt()
        } else {
            5
        }

        val ovulationDay = (avgCycle - 14).coerceAtLeast(1)
        val fertileRange = (ovulationDay - 3)..(ovulationDay + 2)

        return buildDates(
            lastPeriodStart = lastPeriodStart,
            cycleLength = avgCycle,
            periodLength = periodLength,
            ovulationDay = ovulationDay,
            fertileRange = fertileRange,
            history = history,
            today = today
        )
    }

    private fun buildDates(
        lastPeriodStart: LocalDate,
        cycleLength: Int,
        periodLength: Int,
        ovulationDay: Int,
        fertileRange: IntRange,
        history: List<CycleRecord>,
        today: LocalDate
    ): CycleCalendarDates {
        val menstruationDays = mutableListOf<LocalDate>()
        val fertileDays = mutableListOf<LocalDate>()
        val pmsDays = mutableListOf<LocalDate>()
        val predictedDays = mutableListOf<LocalDate>()

        // 1. Real Menstruation Days from History
        history.forEach { record ->
            val start = Instant.ofEpochMilli(record.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
            val end = if (record.endDate > 0) {
                Instant.ofEpochMilli(record.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
            } else {
                start.plusDays(periodLength.toLong())
            }
            var current = start
            while (!current.isAfter(end)) {
                menstruationDays.add(current)
                current = current.plusDays(1)
            }
        }

        // 2. Current Cycle Predictions (relative to lastPeriodStart)
        // we calculate for the current cycle and the next one to fill the calendar
        for (cycleOffset in 0..1) {
            val cycleStart = lastPeriodStart.plusDays(cycleOffset.toLong() * cycleLength)

            // Menstruation
            for (i in 0 until periodLength) {
                menstruationDays.add(cycleStart.plusDays(i.toLong()))
            }

            // Fertile Window
            for (day in fertileRange) {
                fertileDays.add(cycleStart.plusDays(day.toLong()))
            }

            // PMS (5 days before next period)
            for (i in (cycleLength - 5) until cycleLength) {
                pmsDays.add(cycleStart.plusDays(i.toLong()))
            }

            // Predicted next period (the one after this cycle)
            if (cycleOffset == 0) {
                val nextPeriodStart = cycleStart.plusDays(cycleLength.toLong())
                for (i in 0 until periodLength) {
                    predictedDays.add(nextPeriodStart.plusDays(i.toLong()))
                }
            }
        }

        return CycleCalendarDates(
            menstruationDays = menstruationDays.distinct().sorted(),
            fertileDays = fertileDays.distinct().sorted(),
            pmsDays = pmsDays.distinct().sorted(),
            predictedDays = predictedDays.distinct().sorted()
        )
    }

    private fun emptyDates() = CycleCalendarDates(emptyList(), emptyList(), emptyList(), emptyList())
}
