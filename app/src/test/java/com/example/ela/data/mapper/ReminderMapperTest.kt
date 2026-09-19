package com.example.ela.data.mapper

import com.example.ela.data.local.entity.ReminderEntity
import com.example.ela.data.remote.dto.ReminderDto
import com.example.ela.domain.model.Reminder
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderMapperTest {

    @Test
    fun `ReminderEntity toDomain should map correctly`() {
        val entity = ReminderEntity(id = 1, title = "Test", description = "Desc", date = 12345L, type = "Type")
        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.description, domain.description)
        assertEquals(entity.date, domain.date)
        assertEquals(entity.type, domain.type)
    }

    @Test
    fun `Reminder toEntity should map correctly`() {
        val domain = Reminder(id = 1, title = "Test", description = "Desc", date = 12345L, type = "Type")
        val entity = domain.toEntity()
        assertEquals(domain.id, entity.id)
        assertEquals(domain.title, entity.title)
        assertEquals(domain.description, entity.description)
        assertEquals(domain.date, entity.date)
        assertEquals(domain.type, entity.type)
    }

    @Test
    fun `ReminderDto toDomain should map correctly`() {
        val dto = ReminderDto(id = "1", title = "Test", description = "Desc", date = 12345L, type = "Type")
        val domain = dto.toDomain()
        assertEquals(dto.title, domain.title)
        assertEquals(dto.description, domain.description)
        assertEquals(dto.date, domain.date)
        assertEquals(dto.type, domain.type)
    }

    @Test
    fun `Reminder toDto should map correctly`() {
        val domain = Reminder(id = 1, title = "Test", description = "Desc", date = 12345L, type = "Type")
        val dto = domain.toDto()
        assertEquals(domain.id.toString(), dto.id)
        assertEquals(domain.title, dto.title)
        assertEquals(domain.description, dto.description)
        assertEquals(domain.date, dto.date)
        assertEquals(domain.type, dto.type)
    }
}
