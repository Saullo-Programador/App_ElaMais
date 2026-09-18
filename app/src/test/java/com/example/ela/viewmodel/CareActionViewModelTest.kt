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
    fun `load should update state with actions from use case`() = runTest {
        // Preparação
        val phase = CyclePhase.MENSTRUAL
        val actions = listOf(
            CareAction(1, "Action 1", "Desc 1", phase, false),
            CareAction(2, "Action 2", "Desc 2", phase, true)
        )
        every { getCareActionsByPhaseUseCase(phase) } returns flowOf(actions)

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
        viewModel.load(phase)
        advanceUntilIdle()

        // Verificação
        val state = viewModel.state.value
        assertEquals(actions, state.actions)
        assertEquals(phase, state.phase)
        assertEquals(false, state.isLoading)
    }
}
