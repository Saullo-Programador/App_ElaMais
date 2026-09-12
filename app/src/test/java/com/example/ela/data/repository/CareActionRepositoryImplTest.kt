package com.example.ela.data.repository

import app.cash.turbine.test
import com.example.ela.data.local.dao.CareActionDao
import com.example.ela.data.local.entity.CareActionEntity
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CareActionRepositoryImplTest {
    private lateinit var dao: CareActionDao
    private lateinit var repository: CareActionRepositoryImpl

    @Before
    fun setup(){
        dao = mockk()
        repository = CareActionRepositoryImpl(dao)
    }

    @Test
    fun `deve buscar acoes pela fase correta` () = runTest {
        every {
            dao.getByPhase(CyclePhase.MENSTRUAL.name)
        } returns flowOf(emptyList())

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {
                awaitItem()
                awaitComplete()
            }
        verify {
            dao.getByPhase(CyclePhase.MENSTRUAL.name)
        }
    }

    @Test
    fun `deve mapear a entidade do banco para dominio` () = runTest {
        val entity = CareActionEntity(
            id = 100,
            title = "Minha ação",
            description = "Descrição",
            phase = CyclePhase.MENSTRUAL.name,
            isCompleted = true
        )

        every {
            dao.getByPhase(CyclePhase.MENSTRUAL.name)
        } returns flowOf(listOf(entity))

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {
                val result = awaitItem()

                val userAction = result.first {
                    it.id.toInt() == 100
                }

                assertEquals(100, userAction.id)
                assertEquals("Minha ação", userAction.title)
                assertEquals("Descrição",userAction.description)
                assertEquals(CyclePhase.MENSTRUAL, userAction.phase)
                assertTrue(userAction.isCompleted)

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `deve retornar acoes padrao quando estiver vazio` () = runTest {
        every {
            dao.getByPhase(CyclePhase.MENSTRUAL.name)
        }returns flowOf(emptyList())

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {
                val result = awaitItem()

                assertEquals(6, result.size)

                assertTrue(
                    result.any{
                        it.id.toInt() == 1 &&
                        it.title == "Beber água quente"
                    }
                )

                assertTrue(
                    result.any {
                        it.id.toInt() == 2 &&
                        it.title == "Descansar"
                    }
                )

                assertTrue(
                    result.any {
                        it.id.toInt() == 3 &&
                        it.title == "Comer chocolate"
                    }
                )

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `deve usar estado do banco quando acao padrao ja existe`() = runTest {

        val entity = CareActionEntity(
            id = 1,
            title = "Beber água quente",
            description = "Ajuda com as cólicas",
            phase = CyclePhase.MENSTRUAL.name,
            isCompleted = true
        )

        every {
            dao.getByPhase(CyclePhase.MENSTRUAL.name)
        } returns flowOf(listOf(entity))

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {

                val result = awaitItem()

                val action = result.first {
                    it.id.toInt() == 1
                }

                assertEquals("Beber água quente", action.title)
                assertTrue(action.isCompleted)

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `deve manter acoes criadas pelo usuario`() = runTest {

        val entity = CareActionEntity(
            id = 999,
            title = "Minha rotina",
            description = "Minha descrição",
            phase = CyclePhase.MENSTRUAL.name,
            isCompleted = false
        )

        every {
            dao.getByPhase(CyclePhase.MENSTRUAL.name)
        } returns flowOf(listOf(entity))

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {

                val result = awaitItem()

                val userAction = result.first {
                    it.id.toInt() == 999
                }

                assertEquals("Minha rotina", userAction.title)
                assertEquals("Minha descrição", userAction.description)
                assertEquals(CyclePhase.MENSTRUAL, userAction.phase)

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `deve salvar acao no banco de dados`() = runTest {
        val action = CareAction(1, "Título", "Desc", CyclePhase.MENSTRUAL, false)

        coEvery { dao.insert(any()) } returns Unit

        repository.save(action)

        coVerify { dao.insert(any()) }
    }

    @Test
    fun `deve atualizar acao no banco de dados`() = runTest {
        val action = CareAction(1, "Título", "Desc", CyclePhase.MENSTRUAL, true)

        coEvery { dao.update(any()) } returns Unit

        repository.update(action)

        coVerify { dao.update(any()) }
    }

    @Test
    fun `deve resetar todas as acoes concluidas`() = runTest {
        coEvery { dao.resetAllCompletions() } returns Unit

        repository.resetCompletedActions()

        coVerify { dao.resetAllCompletions() }
    }

}
