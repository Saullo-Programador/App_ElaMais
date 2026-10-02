package com.example.ela.core.utils

import com.example.ela.domain.model.CyclePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhaseUtilsTest {

    @Test
    fun `getPhaseTitle deve retornar o titulo correto de cada fase`() {
        assertEquals("Menstrual 🩸", getPhaseTitle(CyclePhase.MENSTRUAL))
        assertEquals("Folicular 🌱", getPhaseTitle(CyclePhase.FOLLICULAR))
        assertEquals("Ovulação 💕", getPhaseTitle(CyclePhase.OVULATION))
        assertEquals("Lútea 🌙", getPhaseTitle(CyclePhase.LUTEAL))
        assertEquals("TPM 😅", getPhaseTitle(CyclePhase.TPM))
    }

    @Test
    fun `todas as fases devem ter titulo nao vazio`() {
        CyclePhase.entries.forEach { phase ->
            assertTrue("Fase $phase sem título", getPhaseTitle(phase).isNotBlank())
        }
    }

    @Test
    fun `cada fase deve ter um titulo diferente`() {
        val titles = CyclePhase.entries.map { getPhaseTitle(it) }

        assertEquals(titles.size, titles.toSet().size)
    }
}
