package com.example.ela.viewmodel

import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.model.Reminder
import com.example.ela.domain.usecase.important_date.GetImportantDatesUseCase
import com.example.ela.domain.usecase.important_date.SaveImportantDateUseCase
import com.example.ela.domain.usecase.reminder.DeleteReminderUseCase
import com.example.ela.domain.usecase.reminder.GetRemindersUseCase
import com.example.ela.domain.usecase.reminder.SaveReminderUseCase
import com.example.ela.ui.screens.reminder.ReminderFilterType
import com.example.ela.ui.screens.reminder.TimelineItem
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
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

    private val reminder = Reminder(1, "Remédio", "Tomar após o café", 123L, "Medicação")
    private val importantDate = ImportantDate(1, "Aniversário", 456L, true)

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

    // ------------------------------------------------------------------
    // saveImportantDate
    // ------------------------------------------------------------------

    @Test
    fun `saveImportantDate deve atualizar a mensagem de sucesso`() = runTest {
        coEvery { saveImportantDateUseCase(importantDate) } returns Unit

        viewModel.saveImportantDate(importantDate)

        assertEquals("Data especial salva com sucesso! ❤️", viewModel.state.value.success)
    }

    @Test
    fun `saveImportantDate deve atualizar a mensagem de erro quando falha`() = runTest {
        coEvery { saveImportantDateUseCase(any()) } throws Exception("Erro ao salvar data")

        viewModel.saveImportantDate(importantDate)

        assertEquals("Erro ao salvar data", viewModel.state.value.error)
    }

    // ------------------------------------------------------------------
    // busca e filtro
    // ------------------------------------------------------------------

    @Test
    fun `onSearchQueryChange deve filtrar lembretes pelo titulo`() {
        every { getRemindersUseCase() } returns flowOf(listOf(reminder, reminder.copy(id = 2, title = "Consulta")))
        every { getImportantDatesUseCase() } returns flowOf(emptyList())
        viewModel = ReminderViewModel(getRemindersUseCase, saveReminderUseCase, deleteReminderUseCase, getImportantDatesUseCase, saveImportantDateUseCase)

        viewModel.onSearchQueryChange("remédio")

        val filtered = viewModel.state.value.filteredEvents
        assertEquals(1, filtered.size)
        assertEquals(reminder.id, (filtered.first() as TimelineItem.HealthReminder).reminder.id)
    }

    @Test
    fun `onSearchQueryChange deve ser case insensitive e buscar tambem na descricao`() {
        every { getRemindersUseCase() } returns flowOf(listOf(reminder))
        every { getImportantDatesUseCase() } returns flowOf(emptyList())
        viewModel = ReminderViewModel(getRemindersUseCase, saveReminderUseCase, deleteReminderUseCase, getImportantDatesUseCase, saveImportantDateUseCase)

        viewModel.onSearchQueryChange("CAFÉ")

        assertEquals(1, viewModel.state.value.filteredEvents.size)
    }

    @Test
    fun `onSearchQueryChange sem correspondencia deve retornar lista vazia`() {
        every { getRemindersUseCase() } returns flowOf(listOf(reminder))
        every { getImportantDatesUseCase() } returns flowOf(listOf(importantDate))
        viewModel = ReminderViewModel(getRemindersUseCase, saveReminderUseCase, deleteReminderUseCase, getImportantDatesUseCase, saveImportantDateUseCase)

        viewModel.onSearchQueryChange("não existe")

        assertTrue(viewModel.state.value.filteredEvents.isEmpty())
    }

    @Test
    fun `onFilterTypeChange REMINDERS deve esconder as datas especiais`() {
        every { getRemindersUseCase() } returns flowOf(listOf(reminder))
        every { getImportantDatesUseCase() } returns flowOf(listOf(importantDate))
        viewModel = ReminderViewModel(getRemindersUseCase, saveReminderUseCase, deleteReminderUseCase, getImportantDatesUseCase, saveImportantDateUseCase)

        viewModel.onFilterTypeChange(ReminderFilterType.REMINDERS)

        val events = viewModel.state.value.filteredEvents
        assertEquals(1, events.size)
        assertTrue(events.first() is TimelineItem.HealthReminder)
    }

    @Test
    fun `onFilterTypeChange SPECIAL_DATES deve esconder os lembretes`() {
        every { getRemindersUseCase() } returns flowOf(listOf(reminder))
        every { getImportantDatesUseCase() } returns flowOf(listOf(importantDate))
        viewModel = ReminderViewModel(getRemindersUseCase, saveReminderUseCase, deleteReminderUseCase, getImportantDatesUseCase, saveImportantDateUseCase)

        viewModel.onFilterTypeChange(ReminderFilterType.SPECIAL_DATES)

        val events = viewModel.state.value.filteredEvents
        assertEquals(1, events.size)
        assertTrue(events.first() is TimelineItem.SpecialDate)
    }

    @Test
    fun `onFilterTypeChange ALL deve mostrar lembretes e datas ordenados pela data`() {
        val reminderDepois = reminder.copy(id = 2, date = 999L)
        every { getRemindersUseCase() } returns flowOf(listOf(reminderDepois))
        every { getImportantDatesUseCase() } returns flowOf(listOf(importantDate)) // date = 456L
        viewModel = ReminderViewModel(getRemindersUseCase, saveReminderUseCase, deleteReminderUseCase, getImportantDatesUseCase, saveImportantDateUseCase)

        viewModel.onFilterTypeChange(ReminderFilterType.ALL)

        val events = viewModel.state.value.filteredEvents
        assertEquals(2, events.size)
        assertTrue(events.first() is TimelineItem.SpecialDate) // 456L vem antes de 999L
    }
}
