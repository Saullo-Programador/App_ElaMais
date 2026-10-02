package com.example.ela.viewmodel

import com.example.ela.domain.model.Preferences
import com.example.ela.domain.usecase.notification.CancelNotificationsUseCase
import com.example.ela.domain.usecase.notification.ScheduleImportantDateNotificationUseCase
import com.example.ela.domain.usecase.preferences.GetPreferencesUseCase
import com.example.ela.domain.usecase.preferences.SavePreferencesUseCase
import com.example.ela.domain.usecase.preferences.UpdateDarkModeUseCase
import com.example.ela.domain.usecase.settings.ClearAllDataUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val clearAllDataUseCase = mockk<ClearAllDataUseCase>()
    private val cancelNotificationsUseCase = mockk<CancelNotificationsUseCase>(relaxed = true)
    private val getPreferencesUseCase = mockk<GetPreferencesUseCase>()
    private val savePreferencesUseCase = mockk<SavePreferencesUseCase>()
    private val scheduleImportantDateNotificationUseCase = mockk<ScheduleImportantDateNotificationUseCase>(relaxed = true)
    private val updateDarkModeUseCase = mockk<UpdateDarkModeUseCase>()

    private val testDispatcher = StandardTestDispatcher()
    private val defaultPrefs = Preferences(notificationsEnabled = true, notificationsTime = "08:00", timesPerDay = 1)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getPreferencesUseCase() } returns flowOf(defaultPrefs)
        coEvery { savePreferencesUseCase(any()) } returns Unit
        coEvery { updateDarkModeUseCase(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(
        clearAllDataUseCase,
        cancelNotificationsUseCase,
        getPreferencesUseCase,
        savePreferencesUseCase,
        scheduleImportantDateNotificationUseCase,
        updateDarkModeUseCase
    )

    @Test
    fun `deve carregar as preferencias ao iniciar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(defaultPrefs, viewModel.uiState.value.preferences)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `toggleNotifications deve salvar as preferencias com o novo valor`() = runTest {
        val saved = slot<Preferences>()
        coEvery { savePreferencesUseCase(capture(saved)) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleNotifications(false)
        advanceUntilIdle()

        assertFalse(saved.captured.notificationsEnabled)
    }

    @Test
    fun `desativar notificacoes deve cancelar todas as notificacoes`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleNotifications(false)
        advanceUntilIdle()

        coVerify(exactly = 1) { cancelNotificationsUseCase.cancelAllNotifications() }
    }

    @Test
    fun `updateNotificationTime deve salvar o novo horario`() = runTest {
        val saved = slot<Preferences>()
        coEvery { savePreferencesUseCase(capture(saved)) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateNotificationTime("21:00")
        advanceUntilIdle()

        assertEquals("21:00", saved.captured.notificationsTime)
    }

    @Test
    fun `updateFrequency aumentar deve incrementar ate o maximo de 3`() = runTest {
        val saved = slot<Preferences>()
        coEvery { savePreferencesUseCase(capture(saved)) } returns Unit
        every { getPreferencesUseCase() } returns flowOf(defaultPrefs.copy(timesPerDay = 3))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFrequency(increase = true)
        advanceUntilIdle()

        // já estava em 3 (o máximo): não deve ter chamado save
        coVerify(exactly = 0) { savePreferencesUseCase(any()) }
    }

    @Test
    fun `updateFrequency diminuir nao deve passar de 1`() = runTest {
        every { getPreferencesUseCase() } returns flowOf(defaultPrefs.copy(timesPerDay = 1))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFrequency(increase = false)
        advanceUntilIdle()

        coVerify(exactly = 0) { savePreferencesUseCase(any()) }
    }

    @Test
    fun `updateFrequency deve aumentar de 1 para 2`() = runTest {
        val saved = slot<Preferences>()
        coEvery { savePreferencesUseCase(capture(saved)) } returns Unit
        every { getPreferencesUseCase() } returns flowOf(defaultPrefs.copy(timesPerDay = 1))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateFrequency(increase = true)
        advanceUntilIdle()

        assertEquals(2, saved.captured.timesPerDay)
    }

    @Test
    fun `toggleDarkMode deve chamar o usecase com o valor recebido`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleDarkMode(true)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateDarkModeUseCase(true) }
    }

    @Test
    fun `clearAllData deve indicar sucesso e ligar isClearingData durante a operacao`() = runTest {
        coEvery { clearAllDataUseCase() } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearAllData()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.dataClearedSuccess)
        assertFalse(viewModel.uiState.value.isClearingData)
    }

    @Test
    fun `clearAllData deve cancelar as notificacoes apos limpar os dados`() = runTest {
        coEvery { clearAllDataUseCase() } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearAllData()
        advanceUntilIdle()

        coVerify(exactly = 1) { cancelNotificationsUseCase.cancelAllNotifications() }
    }

    @Test
    fun `clearAllData deve guardar a mensagem de erro quando falha`() = runTest {
        coEvery { clearAllDataUseCase() } throws RuntimeException("Falha ao limpar")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.clearAllData()
        advanceUntilIdle()

        assertEquals("Falha ao limpar", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isClearingData)
        assertFalse(viewModel.uiState.value.dataClearedSuccess)
    }

    @Test
    fun `dismissSuccessMessage deve zerar a flag de sucesso`() = runTest {
        coEvery { clearAllDataUseCase() } returns Unit
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.clearAllData()
        advanceUntilIdle()

        viewModel.dismissSuccessMessage()

        assertFalse(viewModel.uiState.value.dataClearedSuccess)
    }

    @Test
    fun `dismissErrorMessage deve limpar a mensagem de erro`() = runTest {
        coEvery { clearAllDataUseCase() } throws RuntimeException("Falha")
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.clearAllData()
        advanceUntilIdle()

        viewModel.dismissErrorMessage()

        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onConfirmDeleteDataClick deve abrir a confirmacao de exclusao`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onConfirmDeleteDataClick()

        assertTrue(viewModel.uiState.value.showDeleteConfirmation)
    }

    @Test
    fun `onDismissDeleteConfirmation deve fechar a confirmacao de exclusao`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onConfirmDeleteDataClick()

        viewModel.onDismissDeleteConfirmation()

        assertFalse(viewModel.uiState.value.showDeleteConfirmation)
    }
}
