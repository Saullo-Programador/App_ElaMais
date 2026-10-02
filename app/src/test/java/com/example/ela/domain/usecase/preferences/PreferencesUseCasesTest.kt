package com.example.ela.domain.usecase.preferences

import com.example.ela.domain.model.Preferences
import com.example.ela.domain.repository.PreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreferencesUseCasesTest {

    private val repository = mockk<PreferencesRepository>()

    @Test
    fun `GetPreferences deve retornar as preferencias salvas`() = runTest {
        val prefs = Preferences(id = 1, favoriteFoods = listOf("Pizza"), isDarkMode = true)
        every { repository.getPreferences() } returns flowOf(prefs)

        assertEquals(prefs, GetPreferencesUseCase(repository)().first())
    }

    @Test
    fun `GetPreferences deve retornar null quando nada foi salvo`() = runTest {
        every { repository.getPreferences() } returns flowOf(null)

        assertNull(GetPreferencesUseCase(repository)().first())
    }

    @Test
    fun `SavePreferences deve salvar exatamente as preferencias recebidas`() = runTest {
        val prefs = Preferences(id = 1, notificationsTime = "07:30", timesPerDay = 3)
        coEvery { repository.savePreferences(prefs) } returns Unit

        SavePreferencesUseCase(repository)(prefs)

        coVerify(exactly = 1) { repository.savePreferences(prefs) }
    }

    @Test
    fun `UpdateDarkMode deve ativar o tema escuro`() = runTest {
        coEvery { repository.updateDarkMode(true) } returns Unit

        UpdateDarkModeUseCase(repository)(true)

        coVerify(exactly = 1) { repository.updateDarkMode(true) }
    }

    @Test
    fun `UpdateDarkMode deve desativar o tema escuro`() = runTest {
        coEvery { repository.updateDarkMode(false) } returns Unit

        UpdateDarkModeUseCase(repository)(false)

        coVerify(exactly = 1) { repository.updateDarkMode(false) }
    }
}
