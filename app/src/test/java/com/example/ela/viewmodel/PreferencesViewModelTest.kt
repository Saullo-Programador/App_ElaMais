package com.example.ela.viewmodel

import com.example.ela.domain.model.Preferences
import com.example.ela.domain.usecase.notification.CancelNotificationsUseCase
import com.example.ela.domain.usecase.preferences.GetPreferencesUseCase
import com.example.ela.domain.usecase.preferences.SavePreferencesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModelTest {

    private val getPreferencesUseCase = mockk<GetPreferencesUseCase>()
    private val savePreferencesUseCase = mockk<SavePreferencesUseCase>()
    private val cancelNotificationsUseCase = mockk<CancelNotificationsUseCase>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { savePreferencesUseCase(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = PreferencesViewModel(getPreferencesUseCase, savePreferencesUseCase, cancelNotificationsUseCase)

    @Test
    fun `deve carregar as preferencias ao iniciar`() = runTest {
        val prefs = Preferences(favoriteFoods = listOf("Pizza"))
        every { getPreferencesUseCase() } returns flowOf(prefs)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(prefs, viewModel.state.value.preferences)
    }

    @Test
    fun `save deve persistir as preferencias recebidas`() = runTest {
        every { getPreferencesUseCase() } returns flowOf(Preferences(notificationsEnabled = true))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val novas = Preferences(notificationsEnabled = true, favoriteFoods = listOf("Chocolate"))
        viewModel.save(novas)
        advanceUntilIdle()

        coVerify(exactly = 1) { savePreferencesUseCase(novas) }
    }

    @Test
    fun `save deve cancelar notificacoes quando o usuario desativa`() = runTest {
        every { getPreferencesUseCase() } returns flowOf(Preferences(notificationsEnabled = true))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.save(Preferences(notificationsEnabled = false))
        advanceUntilIdle()

        coVerify(exactly = 1) { cancelNotificationsUseCase.cancelAllNotifications() }
    }

    @Test
    fun `save nao deve cancelar notificacoes quando ja estavam desativadas`() = runTest {
        every { getPreferencesUseCase() } returns flowOf(Preferences(notificationsEnabled = false))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.save(Preferences(notificationsEnabled = false))
        advanceUntilIdle()

        coVerify(exactly = 0) { cancelNotificationsUseCase.cancelAllNotifications() }
    }

    @Test
    fun `save nao deve cancelar notificacoes quando continuam ativadas`() = runTest {
        every { getPreferencesUseCase() } returns flowOf(Preferences(notificationsEnabled = true))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.save(Preferences(notificationsEnabled = true, notificationsTime = "20:00"))
        advanceUntilIdle()

        coVerify(exactly = 0) { cancelNotificationsUseCase.cancelAllNotifications() }
    }
}
