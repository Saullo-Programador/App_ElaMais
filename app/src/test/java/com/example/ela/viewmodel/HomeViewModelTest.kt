package com.example.ela.viewmodel

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.CycleInfo
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.usecase.cycle.GetCycleInfoUseCase
import com.example.ela.domain.usecase.cycle.GetCycleUseCase
import com.example.ela.domain.usecase.cycle_record.GetCycleHistoryUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val getCycleUseCase = mockk<GetCycleUseCase>()
    private val getCycleHistoryUseCase = mockk<GetCycleHistoryUseCase>()
    private val getCycleInfoUseCase = mockk<GetCycleInfoUseCase>()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should observe cycle and update state`() = runTest {
        // Preparação
        val mockCycle = mockk<Cycle>()
        val mockHistory = emptyList<com.example.ela.domain.model.CycleRecord>()
        val mockInfo = CycleInfo(
            currentPhase = CyclePhase.FOLLICULAR,
            daysUntilNextPeriod = 10,
            isFertileWindow = false,
            isPms = false,
            suggestions = emptyList(),
            hasData = true
        )

        every { getCycleUseCase() } returns flowOf(mockCycle)
        every { getCycleHistoryUseCase() } returns flowOf(mockHistory)
        every { getCycleInfoUseCase(mockCycle, mockHistory) } returns mockInfo

        // Ação
        val viewModel = HomeViewModel(getCycleUseCase, getCycleHistoryUseCase, getCycleInfoUseCase)
        
        // Avança o coletor do Flow
        advanceUntilIdle()

        // Verificação
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(mockInfo, state.cycleInfo)
    }
}
