package com.example.ela.data.mapper

import com.example.ela.data.local.entity.CycleRecordEntity
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