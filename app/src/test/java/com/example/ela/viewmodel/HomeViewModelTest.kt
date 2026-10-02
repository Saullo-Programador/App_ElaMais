package com.example.ela.viewmodel

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.CycleCalendarDates
import com.example.ela.domain.model.CycleInfo
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.usecase.cycle.GetCycleCalendarDatesUseCase
import com.example.ela.domain.usecase.cycle.GetCycleInfoUseCase
import com.example.ela.domain.usecase.cycle.GetCycleUseCase
import com.example.ela.domain.usecase.cycle_record.GetCycleHistoryUseCase
import com.example.ela.domain.usecase.cycle_record.SaveCycleRecordUseCase
import com.example.ela.notification.scheduler.NotificationScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val getCycleUseCase = mockk<GetCycleUseCase>()
    private val getCycleHistoryUseCase = mockk<GetCycleHistoryUseCase>()
    private val getCycleInfoUseCase = mockk<GetCycleInfoUseCase>()
    private val getCycleCalendarDatesUseCase = mockk<GetCycleCalendarDatesUseCase>()
    private val saveCycleRecordUseCase = mockk<SaveCycleRecordUseCase>()
    private val notificationScheduler = mockk<NotificationScheduler>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val emptyCalendar = CycleCalendarDates(emptyList(), emptyList(), emptyList(), emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HomeViewModel(
        getCycleUseCase, getCycleHistoryUseCase, getCycleInfoUseCase, getCycleCalendarDatesUseCase,
        saveCycleRecordUseCase,
        notificationScheduler
    )

    @Test
    fun `init should observe cycle and update state`() = runTest {
        // Preparação
        val mockCycle = mockk<Cycle>()
        val mockHistory = emptyList<CycleRecord>()
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
        every { getCycleCalendarDatesUseCase(any(), any()) } returns emptyCalendar

        // Ação
        val viewModel = createViewModel()

        // Avança o coletor do Flow
        advanceUntilIdle()

        // Verificação
        val state = viewModel.state.value
        assertEquals(false, state.isLoading)
        assertEquals(mockInfo, state.cycleInfo)
    }

    @Test
    fun `estado inicial deve indicar carregamento e sem dados`() = runTest {
        every { getCycleUseCase() } returns flow { }
        every { getCycleHistoryUseCase() } returns flow { }

        val viewModel = createViewModel()

        assertEquals(true, viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.cycleInfo)
    }

    @Test
    fun `deve combinar as datas do calendario junto com a info do ciclo`() = runTest {
        val cycle = Cycle(1, 28, 5, 1_000L)
        val info = CycleInfo(CyclePhase.MENSTRUAL, 10, isFertileWindow = false, isPms = false, suggestions = emptyList(), hasData = true)
        val calendar = CycleCalendarDates(listOf(), listOf(), listOf(), listOf())

        every { getCycleUseCase() } returns flowOf(cycle)
        every { getCycleHistoryUseCase() } returns flowOf(emptyList())
        every { getCycleInfoUseCase(cycle, emptyList()) } returns info
        every { getCycleCalendarDatesUseCase(cycle, emptyList()) } returns calendar

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(calendar, viewModel.state.value.calendarDates)
    }

    @Test
    fun `quando o fluxo falha deve guardar a mensagem de erro`() = runTest {
        every { getCycleUseCase() } returns flow { throw RuntimeException("Falha ao ler o ciclo") }
        every { getCycleHistoryUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Falha ao ler o ciclo", viewModel.state.value.error)
    }

    @Test
    fun `onPeriodStarted deve registrar o inicio da menstruacao com a data atual`() = runTest {
        every { getCycleUseCase() } returns flowOf(null)
        every { getCycleHistoryUseCase() } returns flowOf(emptyList())
        every { getCycleInfoUseCase(any(), any()) } returns CycleInfo.empty()
        every { getCycleCalendarDatesUseCase(any(), any()) } returns emptyCalendar
        coEvery { saveCycleRecordUseCase(any()) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPeriodStarted()
        advanceUntilIdle()

        coVerify(exactly = 1) { saveCycleRecordUseCase(any()) }
    }

    @Test
    fun `onPeriodStarted deve cancelar os lembretes de menstruacao pendentes`() = runTest {
        every { getCycleUseCase() } returns flowOf(null)
        every { getCycleHistoryUseCase() } returns flowOf(emptyList())
        every { getCycleInfoUseCase(any(), any()) } returns CycleInfo.empty()
        every { getCycleCalendarDatesUseCase(any(), any()) } returns emptyCalendar
        coEvery { saveCycleRecordUseCase(any()) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onPeriodStarted()
        advanceUntilIdle()

        coVerify(exactly = 1) { notificationScheduler.cancelAllMenstruationReminders() }
    }
}
