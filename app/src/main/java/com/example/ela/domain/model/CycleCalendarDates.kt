package com.example.ela.domain.model

import java.time.LocalDate

data class CycleCalendarDates(
    val menstruationDays: List<LocalDate>,
    val fertileDays: List<LocalDate>,
    val pmsDays: List<LocalDate>,
    val predictedDays: List<LocalDate>
)
