package com.example.ela.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleInfoTest {

    @Test
    fun `empty deve indicar que nao ha dados`() {
        val info = CycleInfo.empty()

        assertFalse(info.hasData)
    }

    @Test
    fun `empty deve ter valores neutros`() {
        val info = CycleInfo.empty()

        assertEquals(CyclePhase.FOLLICULAR, info.currentPhase)
        assertEquals(0, info.daysUntilNextPeriod)
        assertEquals(0, info.daysRemainingInPhase)
        assertEquals(0, info.daysUntilFertileWindow)
        assertEquals(0, info.daysUntilPms)
        assertFalse(info.isFertileWindow)
        assertFalse(info.isPms)
        assertTrue(info.suggestions.isEmpty())
    }

    @Test
    fun `duas instancias empty devem ser iguais`() {
        assertEquals(CycleInfo.empty(), CycleInfo.empty())
    }
}
