package com.example.ela.data.repository

import app.cash.turbine.test
import com.example.ela.data.local.dao.CareActionDao
import com.example.ela.data.local.entity.CareActionEntity
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.CareActionDto
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.model.User
import com.example.ela.domain.repository.AuthRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CareActionRepositoryImplTest {

    private lateinit var dao: CareActionDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var authRepository: AuthRepository
    private lateinit var remote: FirestoreUserCollectionMock
    private lateinit var repository: CareActionRepositoryImpl

    private val action = CareAction(1, "Título", "Desc", CyclePhase.MENSTRUAL, false)

    @Before
    fun setup() {
        dao = mockk()
        firestore = mockk()
        authRepository = mockk()

        every { authRepository.getCurrentUser() } returns User(uid = "uid-1")
        remote = FirestoreUserCollectionMock(firestore, "uid-1", "care_actions")

        repository = CareActionRepositoryImpl(dao, firestore, authRepository)
    }

    private fun givenNoLoggedUser() {
        every { authRepository.getCurrentUser() } returns null
    }

    // ------------------------------------------------------------------
    // getByPhase
    // ------------------------------------------------------------------

    @Test
    fun `deve buscar acoes pela fase correta`() = runTest {
        every { dao.getByPhase(CyclePhase.MENSTRUAL.name) } returns flowOf(emptyList())

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {
                awaitItem()
                awaitComplete()
            }

        verify { dao.getByPhase(CyclePhase.MENSTRUAL.name) }
    }

    @Test
    fun `deve mapear a entidade do banco para dominio`() = runTest {
        val entity = CareActionEntity(
            id = 100,
            title = "Minha ação",
            description = "Descrição",
            phase = CyclePhase.MENSTRUAL.name,
            isCompleted = true
        )
        every { dao.getByPhase(CyclePhase.MENSTRUAL.name) } returns flowOf(listOf(entity))

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {
                val userAction = awaitItem().first { it.id == 100L }

                assertEquals("Minha ação", userAction.title)
                assertEquals("Descrição", userAction.description)
                assertEquals(CyclePhase.MENSTRUAL, userAction.phase)
                assertTrue(userAction.isCompleted)

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `deve retornar acoes padrao quando estiver vazio`() = runTest {
        every { dao.getByPhase(CyclePhase.MENSTRUAL.name) } returns flowOf(emptyList())

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {
                val result = awaitItem()

                assertEquals(6, result.size)
                assertTrue(result.any { it.id == 1L && it.title == "Beber água quente" })
                assertTrue(result.any { it.id == 2L && it.title == "Descansar" })
                assertTrue(result.any { it.id == 3L && it.title == "Comer chocolate" })

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `toda fase deve ter acoes padrao da propria fase`() = runTest {
        CyclePhase.entries.forEach { phase ->
            every { dao.getByPhase(phase.name) } returns flowOf(emptyList())

            repository.getByPhase(phase).test {
                val result = awaitItem()

                assertTrue("Fase $phase sem ações padrão", result.isNotEmpty())
                assertTrue("Fase $phase com ação de outra fase", result.all { it.phase == phase })
                cancelAndIgnoreRemainingEvents()
            }
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
        every { dao.getByPhase(CyclePhase.MENSTRUAL.name) } returns flowOf(listOf(entity))

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {
                val result = awaitItem()
                val saved = result.first { it.id == 1L }

                assertEquals("Beber água quente", saved.title)
                assertTrue(saved.isCompleted)
                // não pode duplicar a ação padrão
                assertEquals(6, result.size)

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `acao padrao alterada no banco deve manter a posicao original na lista`() = runTest {
        val entity = CareActionEntity(
            id = 2,
            title = "Descansar",
            description = "Seu corpo precisa de energia",
            phase = CyclePhase.MENSTRUAL.name,
            isCompleted = true
        )
        every { dao.getByPhase(CyclePhase.MENSTRUAL.name) } returns flowOf(listOf(entity))

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {
                val result = awaitItem()

                assertEquals(2L, result[1].id)
                assertTrue(result[1].isCompleted)

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
        every { dao.getByPhase(CyclePhase.MENSTRUAL.name) } returns flowOf(listOf(entity))

        repository.getByPhase(CyclePhase.MENSTRUAL)
            .test {
                val result = awaitItem()
                val userAction = result.first { it.id == 999L }

                assertEquals("Minha rotina", userAction.title)
                assertEquals("Minha descrição", userAction.description)
                assertEquals(CyclePhase.MENSTRUAL, userAction.phase)
                // as 6 padrão + 1 criada pelo usuário
                assertEquals(7, result.size)

                cancelAndIgnoreRemainingEvents()
            }
    }

    // ------------------------------------------------------------------
    // initializeDefaults
    // ------------------------------------------------------------------

    @Test
    fun `initializeDefaults deve inserir no banco todas as acoes padrao da fase`() = runTest {
        val inserted = slot<List<CareActionEntity>>()
        coEvery { dao.insertAll(capture(inserted)) } returns Unit

        repository.initializeDefaults(CyclePhase.TPM)

        assertTrue(inserted.captured.isNotEmpty())
        assertTrue(inserted.captured.all { it.phase == CyclePhase.TPM.name })
        assertFalse(inserted.captured.any { it.isCompleted })
        assertEquals(inserted.captured.size, inserted.captured.map { it.id }.distinct().size)
    }

    // ------------------------------------------------------------------
    // save
    // ------------------------------------------------------------------

    @Test
    fun `deve salvar acao no banco de dados`() = runTest {
        coEvery { dao.insert(any()) } returns Unit
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.set(any()) } returns okTask()

        repository.save(action)

        coVerify { dao.insert(action.toEntity()) }
    }

    @Test
    fun `save deve enviar a acao para o firebase quando ha usuario logado`() = runTest {
        coEvery { dao.insert(any()) } returns Unit
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.set(any()) } returns okTask()

        repository.save(action)

        verify(atLeast = 1) { docMock.set(any()) }
    }

    @Test
    fun `save nao deve tocar no firebase quando nao ha usuario logado`() = runTest {
        givenNoLoggedUser()
        coEvery { dao.insert(any()) } returns Unit

        repository.save(action)

        coVerify { dao.insert(action.toEntity()) }
        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `save deve manter o dado local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.insert(any()) } returns Unit
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.set(any()) } returns failedTask()

        repository.save(action)

        coVerify(exactly = 1) { dao.insert(action.toEntity()) }
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Test
    fun `deve atualizar acao no banco de dados`() = runTest {
        val completed = action.copy(isCompleted = true)
        coEvery { dao.update(any()) } returns 1
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.set(any()) } returns okTask()

        repository.update(completed)

        coVerify { dao.update(completed.toEntity()) }
    }

    @Test
    fun `update deve enviar a acao atualizada para o firebase`() = runTest {
        val completed = action.copy(isCompleted = true)
        coEvery { dao.update(any()) } returns 1
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.set(any()) } returns okTask()

        repository.update(completed)

        verify(atLeast = 1) { docMock.set(any()) }
    }

    @Test
    fun `update deve manter o dado local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.update(any()) } returns 1
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.set(any()) } returns failedTask()

        repository.update(action)

        coVerify(exactly = 1) { dao.update(any()) }
    }

    // ------------------------------------------------------------------
    // deleteById
    // ------------------------------------------------------------------

    @Test
    fun `deleteById deve remover do banco e do firebase`() = runTest {
        coEvery { dao.deleteById(5L) } returns 1
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.delete() } returns okTask()

        repository.deleteById(5L)

        coVerify(exactly = 1) { dao.deleteById(5L) }
        verify(atLeast = 1) { docMock.delete() }
    }

    @Test
    fun `deleteById sem usuario logado remove apenas do banco local`() = runTest {
        givenNoLoggedUser()
        coEvery { dao.deleteById(5L) } returns 1

        repository.deleteById(5L)

        coVerify(exactly = 1) { dao.deleteById(5L) }
        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `deleteById deve manter a remocao local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.deleteById(5L) } returns 1
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.delete() } returns failedTask()

        repository.deleteById(5L)

        coVerify(exactly = 1) { dao.deleteById(5L) }
    }

    // ------------------------------------------------------------------
    // deleteAll
    // ------------------------------------------------------------------

    @Test
    fun `deleteAll deve limpar o banco e apagar cada documento do firebase`() = runTest {
        coEvery { dao.deleteAll() } returns Unit
        every { remote.collection.get() } returns Tasks.forResult(
            querySnapshot(documentSnapshot("a"), documentSnapshot("b"))
        )
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.delete() } returns okTask()

        repository.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
        verify(atLeast = 1) { docMock.delete() }
    }

    @Test
    fun `deleteAll sem usuario logado limpa apenas o banco local`() = runTest {
        givenNoLoggedUser()
        coEvery { dao.deleteAll() } returns Unit

        repository.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `deleteAll deve limpar o banco local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.deleteAll() } returns Unit
        every { remote.collection.get() } returns failedTaskGeneric<com.google.firebase.firestore.QuerySnapshot>()

        repository.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
    }

    // ------------------------------------------------------------------
    // resetCompletedActions
    // ------------------------------------------------------------------

    @Test
    fun `deve resetar todas as acoes concluidas`() = runTest {
        coEvery { dao.resetAllCompletions() } returns Unit

        repository.resetCompletedActions()

        coVerify { dao.resetAllCompletions() }
    }

    // ------------------------------------------------------------------
    // syncCareActions
    // ------------------------------------------------------------------

    @Test
    fun `sync deve gravar no banco local cada acao vinda do firebase`() = runTest {
        val dto1 = CareActionDto("7", "Exercício", "Leve", "FOLLICULAR", true)
        val dto2 = CareActionDto("8", "Planejar", "Semana", "FOLLICULAR", false)
        val doc1 = documentSnapshot("7")
        val doc2 = documentSnapshot("8")
        every { doc1.toObject(CareActionDto::class.java) } returns dto1
        every { doc2.toObject(CareActionDto::class.java) } returns dto2
        every { remote.collection.get() } returns Tasks.forResult(querySnapshot(doc1, doc2))
        coEvery { dao.insert(any()) } returns Unit

        repository.syncCareActions()

        coVerify(exactly = 1) { dao.insert(dto1.toDomain().toEntity()) }
        coVerify(exactly = 1) { dao.insert(dto2.toDomain().toEntity()) }
    }

    @Test
    fun `sync deve ignorar documentos que nao podem ser convertidos`() = runTest {
        val validDto = CareActionDto("7", "Exercício", "Leve", "FOLLICULAR", true)
        val validDoc = documentSnapshot("7")
        val brokenDoc = documentSnapshot("x")
        every { validDoc.toObject(CareActionDto::class.java) } returns validDto
        every { brokenDoc.toObject(CareActionDto::class.java) } returns null
        every { remote.collection.get() } returns Tasks.forResult(querySnapshot(validDoc, brokenDoc))
        coEvery { dao.insert(any()) } returns Unit

        repository.syncCareActions()

        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `sync sem usuario logado nao deve gravar nada`() = runTest {
        givenNoLoggedUser()

        repository.syncCareActions()

        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `sync nao deve quebrar quando o firebase falha`() = runTest {
        every { remote.collection.get() } returns Tasks.forException<com.google.firebase.firestore.QuerySnapshot>(RuntimeException("Firebase Error"))

        repository.syncCareActions()

        coVerify(exactly = 0) { dao.insert(any()) }
    }
}
