package com.example.ela.viewmodel

import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.usecase.important_date.GetImportantDatesUseCase
import com.example.ela.domain.usecase.important_date.SaveImportantDateUseCase
import com.example.ela.domain.usecase.important_date.SyncImportantDatesUseCase
import com.example.ela.domain.usecase.notification.ScheduleImportantDateNotificationUseCase
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
class ImportantDateViewModelTest {

    private val getImportantDatesUseCase = mockk<GetImportantDatesUseCase>()
    private val saveImportantDateUseCase = mockk<SaveImportantDateUseCase>()
    private val scheduleImportantDateNotificationUseCase = mockk<ScheduleImportantDateNotificationUseCase>(relaxed = true)
    private val syncImportantDatesUseCase = mockk<SyncImportantDatesUseCase>()

    private val testDispatcher = StandardTestDispatcher()
    private val dates = listOf(ImportantDate(1, "Aniversário", 1_000L, true))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getImportantDatesUseCase() } returns flowOf(dates)
        coEvery { syncImportantDatesUseCase() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ImportantDateViewModel(
        getImportantDatesUseCase,
        saveImportantDateUseCase,
        scheduleImportantDateNotificationUseCase,
        syncImportantDatesUseCase
    )

    @Test
    fun `deve sincronizar e carregar as datas ao iniciar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) { syncImportantDatesUseCase() }
        assertEquals(dates, viewModel.state.value.dates)
    }

    @Test
    fun `save deve persistir a data informada`() = runTest {
        val date = ImportantDate(id = 2, title = "Viagem", date = 5_000L, isRecurring = false)
        coEvery { saveImportantDateUseCase(date) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.save(date)
        advanceUntilIdle()

        coVerify(exactly = 1) { saveImportantDateUseCase(date) }
    }

    @Test
    fun `save com id existente deve agendar notificacao usando o id da data`() = runTest {
        val date = ImportantDate(id = 2, title = "Viagem", date = 5_000L, isRecurring = false)
        coEvery { saveImportantDateUseCase(date) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.save(date)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            scheduleImportantDateNotificationUseCase(dateId = 2L, title = "Viagem", dateMillis = 5_000L)
        }
    }

    @Test
    fun `save com id zero deve agendar notificacao usando um id gerado`() = runTest {
        val date = ImportantDate(id = 0, title = "Nova data", date = 5_000L, isRecurring = false)
        coEvery { saveImportantDateUseCase(date) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.save(date)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            scheduleImportantDateNotificationUseCase(dateId = match { it > 0L }, title = "Nova data", dateMillis = 5_000L)
        }
    }
}
