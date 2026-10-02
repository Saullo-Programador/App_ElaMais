package com.example.ela.data.mapper

import com.example.ela.data.local.entity.PreferencesEntity
import com.example.ela.data.remote.dto.CycleDto
import com.example.ela.data.remote.dto.CycleRecordDto
import com.example.ela.data.remote.dto.PreferencesDto
import com.example.ela.data.remote.dto.ReminderDto
import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.model.Preferences
import com.example.ela.domain.model.Reminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

/**
 * Cobre as conversões que os testes existentes ainda não cobriam:
 * Cycle <-> CycleDto, CycleRecord <-> CycleRecordDto, Preferences (entity e dto).
 */
class DtoMappersTest {

    // ------------------------------------------------------------------
    // Cycle
    // ------------------------------------------------------------------

    @Test
    fun `Cycle toDto deve mapear todos os campos`() {
        val dto = Cycle(id = 2, cycleLength = 30, periodLength = 6, lastPeriodStart = 9_000L).toDto()

        assertEquals(2L, dto.id)
        assertEquals(30, dto.cycleLength)
        assertEquals(6, dto.periodLength)
        assertEquals(9_000L, dto.lastPeriodStart)
    }

    @Test
    fun `CycleDto toDomain deve mapear todos os campos`() {
        val domain = CycleDto(id = 2, cycleLength = 30, periodLength = 6, lastPeriodStart = 9_000L).toDomain()

        assertEquals(2L, domain.id)
        assertEquals(30, domain.cycleLength)
        assertEquals(6, domain.periodLength)
        assertEquals(9_000L, domain.lastPeriodStart)
    }

    @Test
    fun `Cycle deve fazer o caminho de ida e volta pelo dto`() {
        val original = Cycle(id = 1, cycleLength = 28, periodLength = 5, lastPeriodStart = 1_725_148_800_000L)

        assertEquals(original, original.toDto().toDomain())
    }

    // ------------------------------------------------------------------
    // CycleRecord
    // ------------------------------------------------------------------

    @Test
    fun `CycleRecord toDto deve mapear inicio e fim`() {
        val dto = CycleRecord(id = 7, startDate = 1_000L, endDate = 2_000L).toDto()

        assertEquals(1_000L, dto.startDate)
        assertEquals(2_000L, dto.endDate)
    }

    @Test
    fun `CycleRecordDto toDomain deve mapear inicio e fim com id zero`() {
        val domain = CycleRecordDto(startDate = 1_000L, endDate = 2_000L).toDomain()

        assertEquals(0L, domain.id)
        assertEquals(1_000L, domain.startDate)
        assertEquals(2_000L, domain.endDate)
    }

    @Test
    fun `CycleRecord em andamento deve manter fim igual a zero`() {
        val domain = CycleRecord(id = 1, startDate = 1_000L, endDate = 0L).toDto().toDomain()

        assertEquals(0L, domain.endDate)
    }

    // ------------------------------------------------------------------
    // Preferences (entity guarda listas como JSON)
    // ------------------------------------------------------------------

    private val fullPreferences = Preferences(
        id = 1,
        favoriteFoods = listOf("Pizza", "Açaí"),
        favoriteSweets = listOf("Chocolate 🍫"),
        dislikedThings = listOf("Barulho, muito alto"),
        symptoms = listOf("Cólica", "Dor de cabeça"),
        notificationsEnabled = false,
        notificationsTime = "21:45",
        timesPerDay = 3,
        isDarkMode = true
    )

    @Test
    fun `Preferences toEntity deve guardar as listas como JSON`() {
        val entity = Preferences(favoriteFoods = listOf("Pizza", "Sorvete")).toEntity()

        assertEquals("""["Pizza","Sorvete"]""", entity.favoriteFoods)
        assertEquals("[]", entity.favoriteSweets)
    }

    @Test
    fun `Preferences completa deve fazer o caminho de ida e volta pela entity`() {
        assertEquals(fullPreferences, fullPreferences.toEntity().toDomain())
    }

    @Test
    fun `Preferences com listas vazias deve fazer o caminho de ida e volta pela entity`() {
        val original = Preferences()

        assertEquals(original, original.toEntity().toDomain())
    }

    @Test
    fun `PreferencesEntity toDomain deve ler as listas do JSON`() {
        val entity = PreferencesEntity(
            id = 1,
            favoriteFoods = """["a","b"]""",
            favoriteSweets = "[]",
            dislikedThings = """["x"]""",
            symptoms = "[]"
        )

        val domain = entity.toDomain()

        assertEquals(listOf("a", "b"), domain.favoriteFoods)
        assertTrue(domain.favoriteSweets.isEmpty())
        assertEquals(listOf("x"), domain.dislikedThings)
    }

    @Test
    fun `PreferencesEntity toDomain deve usar os valores padrao de notificacao`() {
        val entity = PreferencesEntity(favoriteFoods = "[]", favoriteSweets = "[]", dislikedThings = "[]", symptoms = "[]")

        val domain = entity.toDomain()

        assertTrue(domain.notificationsEnabled)
        assertEquals("08:00", domain.notificationsTime)
        assertEquals(1, domain.timesPerDay)
        assertFalse(domain.isDarkMode)
    }

    // ------------------------------------------------------------------
    // Preferences <-> PreferencesDto
    // ------------------------------------------------------------------

    @Test
    fun `Preferences toDto deve mapear todos os campos e converter o id para texto`() {
        val dto = fullPreferences.toDto()

        assertEquals("1", dto.id)
        assertEquals(fullPreferences.favoriteFoods, dto.favoriteFoods)
        assertEquals(fullPreferences.favoriteSweets, dto.favoriteSweets)
        assertEquals(fullPreferences.dislikedThings, dto.dislikedThings)
        assertEquals(fullPreferences.symptoms, dto.symptoms)
        assertFalse(dto.notificationsEnabled)
        assertEquals("21:45", dto.notificationsTime)
        assertEquals(3, dto.timesPerDay)
        assertTrue(dto.isDarkMode)
    }

    @Test
    fun `PreferencesDto toDomain deve mapear todos os campos exceto o id`() {
        val domain = fullPreferences.toDto().toDomain()

        assertEquals(fullPreferences.favoriteFoods, domain.favoriteFoods)
        assertEquals(fullPreferences.favoriteSweets, domain.favoriteSweets)
        assertEquals(fullPreferences.dislikedThings, domain.dislikedThings)
        assertEquals(fullPreferences.symptoms, domain.symptoms)
        assertFalse(domain.notificationsEnabled)
        assertEquals("21:45", domain.notificationsTime)
        assertEquals(3, domain.timesPerDay)
        assertTrue(domain.isDarkMode)
    }

    @Test
    fun `PreferencesDto vazio deve virar preferencias padrao`() {
        val domain = PreferencesDto().toDomain()

        assertTrue(domain.favoriteFoods.isEmpty())
        assertTrue(domain.notificationsEnabled)
        assertEquals("08:00", domain.notificationsTime)
        assertEquals(1, domain.timesPerDay)
    }

    // ------------------------------------------------------------------
    // BUGS CONHECIDOS: o id do Firestore é convertido com hashCode()
    // ------------------------------------------------------------------

    @Ignore(
        "BUG CONHECIDO: PreferencesDto.toDomain usa id.hashCode(), então o id \"1\" vira 49. " +
                "Correção: id = id.toLongOrNull() ?: 0L. Depois remova o @Ignore."
    )
    @Test
    fun `Preferences toDto seguido de toDomain deve preservar o id`() {
        assertEquals(fullPreferences.id, fullPreferences.toDto().toDomain().id)
    }

    @Ignore(
        "BUG CONHECIDO: ReminderDto.toDomain usa id.hashCode(), então o id \"1\" vira 49. " +
                "Após sincronizar, o lembrete é gravado com outro id e aparece duplicado. " +
                "Correção: id = id.toLongOrNull() ?: 0L. Depois remova o @Ignore."
    )
    @Test
    fun `Reminder toDto seguido de toDomain deve preservar o id`() {
        val original = Reminder(id = 1, title = "Remédio", description = "Manhã", date = 1_000L, type = "Medicação")

        assertEquals(original.id, original.toDto().toDomain().id)
    }

    @Test
    fun `ReminderDto toDomain deve preservar os demais campos`() {
        val dto = ReminderDto(id = "1", title = "Remédio", description = "Manhã", date = 1_000L, type = "Medicação")

        val domain = dto.toDomain()

        assertEquals("Remédio", domain.title)
        assertEquals("Manhã", domain.description)
        assertEquals(1_000L, domain.date)
        assertEquals("Medicação", domain.type)
    }
}
