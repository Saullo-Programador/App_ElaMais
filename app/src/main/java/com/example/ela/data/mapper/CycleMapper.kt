package com.example.ela.data.mapper

import com.example.ela.data.local.entity.CycleEntity
import com.example.ela.data.remote.dto.CycleDto
import com.example.ela.domain.model.Cycle

fun CycleEntity.toDomain(): Cycle {
    return Cycle(
        id = id,
        cycleLength = cycleLength,
        periodLength = periodLength,
        lastPeriodStart = lastPeriodStart
    )
}

fun Cycle.toEntity(): CycleEntity {
    return CycleEntity(
        id = id,
        cycleLength = cycleLength,
        periodLength = periodLength,
        lastPeriodStart = lastPeriodStart
    )
}

fun CycleDto.toDomain(): Cycle {
    return Cycle(
        id = id.hashCode().toLong(), // conversão simples
        cycleLength = cycleLength,
        periodLength = periodLength,
        lastPeriodStart = lastPeriodStart
    )
}

fun Cycle.toDto(): CycleDto {
    return CycleDto(
        id = id.toString(),
        cycleLength = cycleLength,
        periodLength = periodLength,
        lastPeriodStart = lastPeriodStart
    )
}