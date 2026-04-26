package com.example.ela.data.mapper

import com.example.ela.data.local.entity.CycleEntity
import com.example.ela.domain.model.Cycle
import org.junit.Assert.assertEquals
import org.junit.Test

class CycleMapperTest {

    @Test
    fun `CycleEntity toDomain should map correctly`() {
        val entity = CycleEntity(
            id = 10,
            cycleLength = 30,
            periodLength = 6,
            lastPeriodStart = 1000L
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.cycleLength, domain.cycleLength)
        assertEquals(entity.periodLength, domain.periodLength)
        assertEquals(entity.lastPeriodStart, domain.lastPeriodStart)
    }

    @Test
    fun `Cycle toEntity should map correctly`() {
        val domain = Cycle(
            id = 5,
            cycleLength = 28,
            periodLength = 4,
            lastPeriodStart = 2000L
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.cycleLength, entity.cycleLength)
        assertEquals(domain.periodLength, entity.periodLength)
        assertEquals(domain.lastPeriodStart, entity.lastPeriodStart)
    }
}
