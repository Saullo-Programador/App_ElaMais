package com.example.ela.viewmodel

import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.model.Reminder
import com.example.ela.domain.usecase.important_date.GetImportantDatesUseCase
import com.example.ela.domain.usecase.important_date.SaveImportantDateUseCase
import com.example.ela.domain.usecase.reminder.DeleteReminderUseCase
import com.example.ela.domain.usecase.reminder.GetRemindersUseCase
import com.example.ela.domain.usecase.reminder.SaveReminderUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderViewModelTest {

    private val getRemindersUseCase: GetRemindersUseCase = mockk()
    private val saveReminderUseCase: SaveReminderUseCase = mockk()
    private val deleteReminderUseCase: DeleteReminderUseCase = mockk()
    private val getImportantDatesUseCase: GetImportantDatesUseCase = mockk()
    private val saveImportantDateUseCase: SaveImportantDateUseCase = mockk()

    private lateinit var viewModel: ReminderViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { getRemindersUseCase() } returns flowOf(emptyList())
        every { getImportantDatesUseCase() } returns flowOf(emptyList())

        viewModel = ReminderViewModel(
            getRemindersUseCase,
            saveReminderUseCase,
            deleteReminderUseCase,
            getImportantDatesUseCase,
            saveImportantDateUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should load reminders and dates on init`() {
        val reminders = listOf(Reminder(1, "T1", "D1", 123L, "Type"))
        val dates = listOf(ImportantDate(1, "D1", 456L, true))

        every { getRemindersUseCase() } returns flowOf(reminders)
        every { getImportantDatesUseCase() } returns flowOf(dates)

        // Re-init to trigger load with new mocks
        viewModel = ReminderViewModel(getRemindersUseCase, saveReminderUseCase, deleteReminderUseCase, getImportantDatesUseCase, saveImportantDateUseCase)

        assertEquals(reminders, viewModel.state.value.reminders)
        assertEquals(dates, viewModel.state.value.importantDates)
        assertEquals(false, viewModel.state.value.isLoading)
    }

    @Test
    fun `should update success message when saving reminder succeeds`() = runTest {
        val reminder = Reminder(1, "T1", "D1", 123L, "Type")
        coEvery { saveReminderUseCase(reminder) } returns Unit

        viewModel.save(reminder)

        assertEquals("Lembrete salvo com sucesso! 🔔", viewModel.state.value.success)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun `should update error message when saving reminder fails`() = runTest {
        val reminder = Reminder(1, "T1", "D1", 123L, "Type")
        coEvery { saveReminderUseCase(reminder) } throws Exception("Erro de teste")

        viewModel.save(reminder)

        assertEquals("Erro de teste", viewModel.state.value.error)
        assertEquals(null, viewModel.state.value.success)
    }

    @Test
    fun `should update success message when deleting reminder succeeds`() = runTest {
        val reminder = Reminder(1, "T1", "D1", 123L, "Type")
        coEvery { deleteReminderUseCase(reminder) } returns Unit

        viewModel.delete(reminder)

        assertEquals("Lembrete removido com sucesso! 🗑️", viewModel.state.value.success)
    }

    @Test
    fun `should clear messages when clearMessage is called`() = runTest {
        // Setup a state with messages
        coEvery { saveReminderUseCase(any()) } returns Unit
        viewModel.save(Reminder(1, "T", "D", 1L, "Type"))

        viewModel.clearMessage()

        assertEquals(null, viewModel.state.value.success)
        assertEquals(null, viewModel.state.value.error)
    }
}
