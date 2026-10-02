package com.example.ela.viewmodel

import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.repository.CycleRecordRepository
import com.example.ela.domain.usecase.cycle_record.DeleteCycleRecordUseCase
import com.example.ela.domain.usecase.cycle_record.GetCycleHistoryUseCase
import com.example.ela.domain.usecase.cycle_record.SaveCycleRecordUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CycleHistoryViewModelTest {

    private val getCycleHistoryUseCase = mockk<GetCycleHistoryUseCase>()
    private val saveCycleRecordUseCase = mockk<SaveCycleRecordUseCase>()
    private val deleteCycleRecordUseCase = mockk<DeleteCycleRecordUseCase>()
    private val cycleRecordRepository = mockk<CycleRecordRepository>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val history = listOf(CycleRecord(1, 1_000L, 2_000L), CycleRecord(2, 3_000L, 0L))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getCycleHistoryUseCase() } returns flowOf(history)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = CycleHistoryViewModel(
        getCycleHistoryUseCase,
        saveCycleRecordUseCase,
        deleteCycleRecordUseCase,
        cycleRecordRepository
    )

    @Test
    fun `deve carregar o historico ao iniciar`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(history, viewModel.state.value.history)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `saveRecord com id zero deve usar o SaveCycleRecordUseCase`() = runTest {
        coEvery { saveCycleRecordUseCase(any(), any()) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.saveRecord(start = 1_000L, end = 2_000L, id = 0)
        advanceUntilIdle()

        coVerify(exactly = 1) { saveCycleRecordUseCase(1_000L, 2_000L) }
        coVerify(exactly = 0) { cycleRecordRepository.save(any()) }
    }

    @Test
    fun `saveRecord com id existente deve editar direto pelo repositorio`() = runTest {
        coEvery { cycleRecordRepository.save(any()) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.saveRecord(start = 1_000L, end = 2_000L, id = 7)
        advanceUntilIdle()

        coVerify(exactly = 1) { cycleRecordRepository.save(CycleRecord(id = 7, startDate = 1_000L, endDate = 2_000L)) }
        coVerify(exactly = 0) { saveCycleRecordUseCase(any(), any()) }
    }

    @Test
    fun `deleteRecord deve chamar o DeleteCycleRecordUseCase`() = runTest {
        val record = history.first()
        coEvery { deleteCycleRecordUseCase(record) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteRecord(record)
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteCycleRecordUseCase(record) }
    }

    @Test
    fun `deleteAllRecords deve limpar todo o historico pelo repositorio`() = runTest {
        coEvery { cycleRecordRepository.deleteAll() } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteAllRecords()
        advanceUntilIdle()

        coVerify(exactly = 1) { cycleRecordRepository.deleteAll() }
    }
}
