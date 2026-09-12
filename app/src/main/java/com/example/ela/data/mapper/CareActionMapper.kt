package com.example.ela.data.mapper

import com.example.ela.data.local.entity.CareActionEntity
import com.example.ela.data.remote.dto.CareActionDto
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase


fun CareActionEntity.toDomain(): CareAction {
    return CareAction(
        id = id,
        title = title,
        description = description,
        phase = CyclePhase.valueOf(phase),
        isCompleted = isCompleted
    )
}

fun CareAction.toEntity(): CareActionEntity {
    return CareActionEntity(
        id = id,
        title = title,
        description = description,
        phase = phase.name,
        isCompleted = isCompleted
    )
}

fun CareActionDto.toDomain(): CareAction {
    return CareAction(
        id = id.hashCode().toLong(),
        title = title,
        description = description,
        phase = CyclePhase.valueOf(phase),
        isCompleted = isCompleted
    )
}

fun CareAction.toDto(): CareActionDto {
    return CareActionDto(
        id = id.toString(),
        title = title,
        description = description,
        phase = phase.name,
        isCompleted = isCompleted
    )
}