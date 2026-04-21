package com.example.ela.data.mapper

import com.example.ela.data.local.entity.ImportantDateEntity
import com.example.ela.data.remote.dto.ImportantDateDto
import com.example.ela.domain.model.ImportantDate


fun ImportantDateEntity.toDomain(): ImportantDate {
    return ImportantDate(
        id = id,
        title = title,
        date = date,
        isRecurring = isRecurring
    )
}

fun ImportantDate.toEntity(): ImportantDateEntity {
    return ImportantDateEntity(
        id = id,
        title = title,
        date = date,
        isRecurring = isRecurring
    )
}

fun ImportantDateDto.toDomain(): ImportantDate {
    return ImportantDate(
        id = id.hashCode().toLong(),
        title = title,
        date = date,
        isRecurring = isRecurring
    )
}

fun ImportantDate.toDto(): ImportantDateDto {
    return ImportantDateDto(
        id = id.toString(),
        title = title,
        date = date,
        isRecurring = isRecurring
    )
}