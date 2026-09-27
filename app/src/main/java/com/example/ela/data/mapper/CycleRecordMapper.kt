package com.example.ela.data.mapper

import com.example.ela.data.local.entity.CycleRecordEntity
import com.example.ela.data.remote.dto.CycleRecordDto
import com.example.ela.domain.model.CycleRecord

fun CycleRecordEntity.toDomain() = CycleRecord(
    id = id,
    startDate = startDate,
    endDate = endDate
)

fun CycleRecord.toEntity() = CycleRecordEntity(
    id = id,
    startDate = startDate,
    endDate = endDate
)

fun CycleRecordDto.toDomain(): CycleRecord {
    return CycleRecord(
        id = 0,
        startDate = startDate,
        endDate = endDate
    )
}

fun CycleRecord.toDto(): CycleRecordDto {
    return CycleRecordDto(
        startDate = startDate,
        endDate = endDate
    )
}