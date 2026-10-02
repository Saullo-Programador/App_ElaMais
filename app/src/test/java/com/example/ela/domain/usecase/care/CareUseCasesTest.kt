package com.example.ela.domain.usecase.care

import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.repository.CareActionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CareUseCasesTest {

    private val repository = mockk<CareActionRepository>()

    @Test
    fun `GetCareActionsByPhase deve buscar as acoes da fase pedida`() = runTest {
        val actions = listOf(CareAction(1, "Descansar", "Desc", CyclePhase.MENSTRUAL, false))
        every { repository.getByPhase(CyclePhase.MENSTRUAL) } returns flowOf(actions)

        val result = GetCareActionsByPhaseUseCase(repository)(CyclePhase.MENSTRUAL).first()

        assertEquals(actions, result)
    }

    @Test
    fun `GetCareActionsByPhase deve retornar lista vazia quando a fase nao tem acoes`() = runTest {
        every { repository.getByPhase(CyclePhase.TPM) } returns flowOf(emptyList())

        val result = GetCareActionsByPhaseUseCase(repository)(CyclePhase.TPM).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `DeleteCareActions deve remover pelo id`() = runTest {
        coEvery { repository.deleteById(5L) } returns Unit

        DeleteCareActionsUseCase(repository)(5L)

        coVerify(exactly = 1) { repository.deleteById(5L) }
    }

    @Test
    fun `DeleteAllCareActions deve limpar todos os cuidados`() = runTest {
        coEvery { repository.deleteAll() } returns Unit

        DeleteAllCareActionsUseCase(repository)()

        coVerify(exactly = 1) { repository.deleteAll() }
    }

    @Test
    fun `InitializeCareActions deve criar os cuidados padrao da fase`() = runTest {
        coEvery { repository.initializeDefaults(CyclePhase.LUTEAL) } returns Unit

        InitializeCareActionsUseCase(repository)(CyclePhase.LUTEAL)

        coVerify(exactly = 1) { repository.initializeDefaults(CyclePhase.LUTEAL) }
    }

    @Test
    fun `ResetCompletedActions deve desmarcar os cuidados concluidos`() = runTest {
        coEvery { repository.resetCompletedActions() } returns Unit

        ResetCompletedActionsUseCase(repository)()

        coVerify(exactly = 1) { repository.resetCompletedActions() }
    }

    @Test
    fun `SyncCareActions deve sincronizar com a nuvem`() = runTest {
        coEvery { repository.syncCareActions() } returns Unit

        SyncCareActionsUseCase(repository)()

        coVerify(exactly = 1) { repository.syncCareActions() }
    }
}
