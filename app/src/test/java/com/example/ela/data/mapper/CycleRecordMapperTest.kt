package com.example.ela.data.mapper

import com.example.ela.data.local.entity.CycleRecordEntity
import com.example.ela.domain.model.CycleRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class CycleRecordMapperTest {

    @Test
    fun `CycleRecordEntity toDomain should map correctly`() {
        val entity = CycleRecordEntity(id = 1, startDate = 1000L, endDate = 2000L)
        val domain = entity.toDomain()
        
        assertEquals(entity.id, domain.id)
        assertEquals(entity.startDate, domain.startDate)
        assertEquals(entity.endDate, domain.endDate)
    }

    @Test
    fun `CycleRecord toEntity should map correctly`() {
        val domain = CycleRecord(id = 1, startDate = 1000L, endDate = 2000L)
        val entity = domain.toEntity()
        
        assertEquals(domain.id, entity.id)
        assertEquals(domain.startDate, entity.startDate)
        assertEquals(domain.endDate, entity.endDate)
    }
}
