package com.example.ela.data.mapper

import com.example.ela.data.local.entity.CareActionEntity
import com.example.ela.data.remote.dto.CareActionDto
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import org.junit.Assert.assertEquals
import org.junit.Test

class CareActionMapperTest {

    @Test
    fun `CareActionEntity toDomain should map correctly`() {
        val entity = CareActionEntity(id = 1, title = "Title", description = "Desc", phase = "MENSTRUAL", isCompleted = true)
        val domain = entity.toDomain()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.description, domain.description)
        assertEquals(CyclePhase.MENSTRUAL, domain.phase)
        assertEquals(entity.isCompleted, domain.isCompleted)
    }

    @Test
    fun `CareAction toEntity should map correctly`() {
        val domain = CareAction(id = 1, title = "Title", description = "Desc", phase = CyclePhase.MENSTRUAL, isCompleted = true)
        val entity = domain.toEntity()
        assertEquals(domain.id, entity.id)
        assertEquals(domain.title, entity.title)
        assertEquals(domain.description, entity.description)
        assertEquals(CyclePhase.MENSTRUAL.name, entity.phase)
        assertEquals(domain.isCompleted, entity.isCompleted)
    }

    @Test
    fun `CareActionDto toDomain should map correctly`() {
        val dto = CareActionDto(id = "1", title = "Title", description = "Desc", phase = "MENSTRUAL", isCompleted = true)
        val domain = dto.toDomain()
        assertEquals(1L, domain.id)
        assertEquals(dto.title, domain.title)
        assertEquals(dto.description, domain.description)
        assertEquals(CyclePhase.MENSTRUAL, domain.phase)
        assertEquals(dto.isCompleted, domain.isCompleted)
    }

    @Test
    fun `CareAction toDto should map correctly`() {
        val domain = CareAction(id = 1, title = "Title", description = "Desc", phase = CyclePhase.MENSTRUAL, isCompleted = true)
        val dto = domain.toDto()
        assertEquals("1", dto.id)
        assertEquals(domain.title, dto.title)
        assertEquals(domain.description, dto.description)
        assertEquals(CyclePhase.MENSTRUAL.name, dto.phase)
        assertEquals(domain.isCompleted, dto.isCompleted)
    }
}
