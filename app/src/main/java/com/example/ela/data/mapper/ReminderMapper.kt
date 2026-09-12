package com.example.ela.data.mapper

import com.example.ela.data.local.entity.ReminderEntity
import com.example.ela.data.remote.dto.ReminderDto
import com.example.ela.domain.model.Reminder


fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = id,
        title = title,
        description = description,
        date = date,
        type = type,
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        title = title,
        description = description,
        date = date,
        type = type,
    )
}

fun ReminderDto.toDomain(): Reminder {
    return Reminder(
        id = id.hashCode().toLong(),
        title = title,
        description = description,
        date = date,
        type = type,
    )
}

fun Reminder.toDto(): ReminderDto {
    return ReminderDto(
        id = id.toString(),
        title = title,
        description = description,
        date = date,
        type = type,
    )
}