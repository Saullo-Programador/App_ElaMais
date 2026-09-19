package com.example.ela.viewmodel

import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.usecase.care.DeleteAllCareActionsUseCase
import com.example.ela.domain.usecase.care.DeleteCareActionsUseCase
import com.example.ela.domain.usecase.care.GetCareActionsByPhaseUseCase
import com.example.ela.domain.usecase.care.InitializeCareActionsUseCase
import com.example.ela.domain.usecase.care.SaveCareActionUseCase
import com.example.ela.domain.usecase.care.SyncCareActionsUseCase
import com.example.ela.domain.usecase.care.UpdateCareActionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CareActionViewModelTest {

    private val getCareActionsByPhaseUseCase = mockk<GetCareActionsByPhaseUseCase>()
    private val updateCareActionUseCase = mockk<UpdateCareActionUseCase>()
    private val saveCareActionUseCase = mockk<SaveCareActionUseCase>()
    private val deleteCareActionsUseCase = mockk<DeleteCareActionsUseCase>()
    private val deleteAllCareActionsUseCase = mockk<DeleteAllCareActionsUseCase>()
    private val syncCareActionsUseCase = mockk<SyncCareActionsUseCase>()
    private val initializeCareActionsUseCase = mockk<InitializeCareActionsUseCase>()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { syncCareActionsUseCase() } returns Unit
        coEvery { initializeCareActionsUseCase(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleDone should invert isCompleted and call updateCareActionUseCase`() = runTest {
        // Preparação
        val action = CareAction(
            id = 1,
            title = "Test Action",
            description = "Test Description",
            phase = CyclePhase.MENSTRUAL,
            isCompleted = false
        )
        coEvery { updateCareActionUseCase(any()) } returns Unit

        val viewModel = CareActionViewModel(
            getCareActionsByPhaseUseCase,
            updateCareActionUseCase,
            saveCareActionUseCase,
            deleteCareActionsUseCase,
            deleteAllCareActionsUseCase,
            initializeCareActionsUseCase,
            syncCareActionsUseCase
        )

        // Ação
        viewModel.toggleDone(action)
        advanceUntilIdle()

        // Verificação
        coVerify {
            updateCareActionUseCase(match { it.id == action.id && it.isCompleted == true })
        }
    }

    @Test
    fun `toggleDone should unset isCompleted and call updateCareActionUseCase`() = runTest {
        // Preparação
        val action = CareAction(
            id = 1,
            title = "Test Action",
            description = "Test Description",
            phase = CyclePhase.MENSTRUAL,
            isCompleted = true
        )
        coEvery { updateCareActionUseCase(any()) } returns Unit

        val viewModel = CareActionViewModel(
            getCareActionsByPhaseUseCase,
            updateCareActionUseCase,
            saveCareActionUseCase,
            deleteCareActionsUseCase,
            deleteAllCareActionsUseCase,
            initializeCareActionsUseCase,
            syncCareActionsUseCase
        )

        // Ação
        viewModel.toggleDone(action)
        advanceUntilIdle()

        // Verificação
        coVerify {
            updateCareActionUseCase(match { it.id == action.id && it.isCompleted == false })
        }
    }

    @Test
    fun `save should update success message when saving action succeeds`() = runTest {
        val action = CareAction(1, "T", "D", CyclePhase.MENSTRUAL, false)
        coEvery { saveCareActionUseCase(action) } returns Unit

        val viewModel = createViewModel()

        viewModel.save(action)
        advanceUntilIdle()

        assertEquals("Cuidado salvo com sucesso! ✨", viewModel.state.value.success)
    }

    @Test
    fun `delete should update success message when deleting action succeeds`() = runTest {
        val id = 1L
        coEvery { deleteCareActionsUseCase(id) } returns Unit

        val viewModel = createViewModel()

        viewModel.delete(id)
        advanceUntilIdle()

        assertEquals("Cuidado removido com sucesso! 🗑️", viewModel.state.value.success)
    }

    @Test
    fun `deleteAll should update success message when deleting all succeeds`() = runTest {
        coEvery { deleteAllCareActionsUseCase() } returns Unit

        val viewModel = createViewModel()

        viewModel.deleteAll()
        advanceUntilIdle()

        assertEquals("Todos os cuidados foram removidos! 🧹", viewModel.state.value.success)
    }

    @Test
    fun `clearMessage should reset success and error messages`() = runTest {
        val viewModel = createViewModel()

        // Trigger a success message first
        coEvery { saveCareActionUseCase(any()) } returns Unit
        viewModel.save(CareAction(1, "T", "D", CyclePhase.MENSTRUAL, false))
        advanceUntilIdle()

        viewModel.clearMessage()

        assertEquals(null, viewModel.state.value.success)
        assertEquals(null, viewModel.state.value.error)
    }

    private fun createViewModel() = CareActionViewModel(
        getCareActionsByPhaseUseCase,
        updateCareActionUseCase,
        saveCareActionUseCase,
        deleteCareActionsUseCase,
        deleteAllCareActionsUseCase,
        initializeCareActionsUseCase,
        syncCareActionsUseCase
    )

}
