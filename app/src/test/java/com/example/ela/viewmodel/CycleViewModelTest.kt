package com.example.ela.viewmodel

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.usecase.cycle.GetCycleUseCase
import com.example.ela.domain.usecase.cycle.SaveCycleUseCase
import com.example.ela.domain.usecase.notification.ScheduleCycleNotificationsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CycleViewModelTest {

    private val getCycleUseCase = mockk<GetCycleUseCase>()
    private val saveCycleUseCase = mockk<SaveCycleUseCase>()
    private val scheduleCycleNotificationsUseCase = mockk<ScheduleCycleNotificationsUseCase>()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getCycleUseCase() } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveCycle should call useCase and schedule notifications`() = runTest {
        val cycle = Cycle(id = 1, cycleLength = 28, periodLength = 5, lastPeriodStart = 1000L)
        coEvery { saveCycleUseCase(any()) } returns Unit
        coEvery { scheduleCycleNotificationsUseCase(any(), any(), any()) } returns Unit

        val viewModel = CycleViewModel(getCycleUseCase, saveCycleUseCase, scheduleCycleNotificationsUseCase)
        viewModel.saveCycle(cycle)
        
        advanceUntilIdle()

        coVerify { saveCycleUseCase(cycle) }
        coVerify { scheduleCycleNotificationsUseCase(any(), any(), any()) }
        assertTrue(viewModel.state.value.success)
    }

    @Test
    fun `saveCycle error should update state with error`() = runTest {
        val cycle = Cycle(id = 1, cycleLength = 28, periodLength = 5, lastPeriodStart = 1000L)
        val errorMessage = "Erro ao salvar"
        coEvery { saveCycleUseCase(any()) } throws Exception(errorMessage)

        val viewModel = CycleViewModel(getCycleUseCase, saveCycleUseCase, scheduleCycleNotificationsUseCase)
        viewModel.saveCycle(cycle)
        
        advanceUntilIdle()

        assertEquals(errorMessage, viewModel.state.value.error)
        assertEquals(false, viewModel.state.value.isSaving)
    }
}
