package com.example.ela.data.repository

import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.local.entity.CycleEntity
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.CycleDto
import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.User
import com.example.ela.domain.repository.AuthRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CycleRepositoryImplTest {

    private lateinit var dao: CycleDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var authRepository: AuthRepository
    private lateinit var remote: FirestoreUserCollectionMock
    private lateinit var repository: CycleRepositoryImpl

    private val cycle = Cycle(id = 1, cycleLength = 28, periodLength = 5, lastPeriodStart = 1_725_148_800_000L)

    @Before
    fun setup() {
        dao = mockk()
        firestore = mockk()
        authRepository = mockk()

        every { authRepository.getCurrentUser() } returns User(uid = "uid-1")
        remote = FirestoreUserCollectionMock(firestore, "uid-1", "cycles")

        repository = CycleRepositoryImpl(dao, firestore, authRepository)
    }

    private fun givenNoLoggedUser() {
        every { authRepository.getCurrentUser() } returns null
    }

    // ------------------------------------------------------------------
    // getCycle
    // ------------------------------------------------------------------

    @Test
    fun `deve mapear cycle entity para domain`() = runTest {
        val entity = CycleEntity(id = 10, cycleLength = 30, periodLength = 6, lastPeriodStart = 1_000L)
        every { dao.getCycle() } returns flowOf(entity)

        val result = repository.getCycle().first()

        assertEquals(entity.toDomain(), result)
    }

    @Test
    fun `deve retornar null quando nao existir ciclo`() = runTest {
        every { dao.getCycle() } returns flowOf(null)

        assertNull(repository.getCycle().first())
    }

    // ------------------------------------------------------------------
    // saveCycle
    // ------------------------------------------------------------------

    @Test
    fun `deve salvar ciclo localmente e tentar salvar no firebase`() = runTest {
        val document = remote.document("current")
        coEvery { dao.insertCycle(any()) } returns Unit
        every { document.set(any()) } returns Tasks.forResult(null)

        repository.saveCycle(cycle)

        coVerify { dao.insertCycle(cycle.toEntity()) }
        verify { document.set(cycle.toDto()) }
    }

    @Test
    fun `deve salvar localmente mesmo se firebase falhar`() = runTest {
        val document = remote.document("current")
        coEvery { dao.insertCycle(any()) } returns Unit
        every { document.set(any()) } throws RuntimeException("Firebase Error")

        repository.saveCycle(cycle)

        coVerify { dao.insertCycle(any()) }
    }

    @Test
    fun `saveCycle sem usuario logado nao deve tocar no firebase`() = runTest {
        givenNoLoggedUser()
        coEvery { dao.insertCycle(any()) } returns Unit

        repository.saveCycle(cycle)

        coVerify { dao.insertCycle(cycle.toEntity()) }
        verify(exactly = 0) { firestore.collection(any()) }
    }

    // ------------------------------------------------------------------
    // syncCycle
    // ------------------------------------------------------------------

    @Test
    fun `deve sincronizar ciclo do firebase para o banco local`() = runTest {
        val dto = CycleDto(id = 1, cycleLength = 30, periodLength = 6, lastPeriodStart = 1_725_148_800_000L)
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(CycleDto::class.java) } returns dto

        val document = remote.document("current")
        every { document.get() } returns Tasks.forResult(snapshot)
        coEvery { dao.insertCycle(any()) } returns Unit

        repository.syncCycle()

        coVerify {
            dao.insertCycle(
                match {
                    it.id == 1L && it.cycleLength == 30 && it.periodLength == 6 &&
                            it.lastPeriodStart == 1_725_148_800_000L
                }
            )
        }
    }

    @Test
    fun `sync nao deve salvar nada quando o documento nao existe`() = runTest {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(CycleDto::class.java) } returns null

        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.get() } returns Tasks.forResult(snapshot)

        repository.syncCycle()

        coVerify(exactly = 0) { dao.insertCycle(any()) }
    }

    @Test
    fun `sync nao deve quebrar quando o firebase falha`() = runTest {
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.get() } returns failedTaskGeneric<DocumentSnapshot>()

        repository.syncCycle()

        coVerify(exactly = 0) { dao.insertCycle(any()) }
    }

    @Test
    fun `sync sem usuario logado nao deve gravar nada`() = runTest {
        givenNoLoggedUser()

        repository.syncCycle()

        coVerify(exactly = 0) { dao.insertCycle(any()) }
    }

    // ------------------------------------------------------------------
    // deleteAll
    // ------------------------------------------------------------------

    @Test
    fun `deleteAll deve limpar o banco local e o documento no firebase`() = runTest {
        coEvery { dao.deleteAll() } returns Unit
        val document = remote.document("current")
        every { document.delete() } returns Tasks.forResult(null)

        repository.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
        verify(exactly = 1) { document.delete() }
    }

    @Test
    fun `deleteAll deve limpar o banco local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.deleteAll() } returns Unit
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.delete() } returns failedTask()

        repository.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
    }

    @Test
    fun `deleteAll sem usuario logado limpa apenas o banco local`() = runTest {
        givenNoLoggedUser()
        coEvery { dao.deleteAll() } returns Unit

        repository.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
        verify(exactly = 0) { firestore.collection(any()) }
    }
}
