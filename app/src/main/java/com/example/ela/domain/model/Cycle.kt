package com.example.ela.domain.model

data class Cycle(
    val id: Long,
    val cycleLength: Int,
    val periodLength: Int,
    val lastPeriodStart: Long
)

data class CycleRecord(
    val id: Long,
    val startDate: Long,
    val endDate: Long
)

data class CycleHistory(
    val cycles: List<CycleRecord>
)