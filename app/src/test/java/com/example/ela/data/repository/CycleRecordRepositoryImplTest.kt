package com.example.ela.data.repository

import com.example.ela.data.local.dao.CycleRecordDao
import com.example.ela.data.local.entity.CycleRecordEntity
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.CycleRecordDto
import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.model.User
import com.example.ela.domain.repository.AuthRepository
import com.google.android.gms.tasks.Tasks
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
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CycleRecordRepositoryImplTest {

    private lateinit var dao: CycleRecordDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var authRepository: AuthRepository
    private lateinit var remote: FirestoreUserCollectionMock
    private lateinit var repository: CycleRecordRepositoryImpl

    private val startDate = 1_725_148_800_000L // data fixa, usada para montar o documentId esperado
    private val record = CycleRecord(id = 1, startDate = startDate, endDate = startDate + 100_000L)

    /** Mesmo formato usado internamente pelo repositório para gerar o id do documento. */
    private val documentId = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(startDate))

    @Before
    fun setup() {
        dao = mockk()
        firestore = mockk()
        authRepository = mockk()

        every { authRepository.getCurrentUser() } returns User(uid = "uid-1")
        remote = FirestoreUserCollectionMock(firestore, "uid-1", "cycles")

        repository = CycleRecordRepositoryImpl(dao, firestore, authRepository)
    }

    private fun givenNoLoggedUser() {
        every { authRepository.getCurrentUser() } returns null
    }

    // ------------------------------------------------------------------
    // getHistory
    // ------------------------------------------------------------------

    @Test
    fun `deve retornar o historico mapeado do banco`() = runTest {
        val entity = CycleRecordEntity(id = 1, startDate = 1_000L, endDate = 2_000L)
        every { dao.getAll() } returns flowOf(listOf(entity))

        val result = repository.getHistory().first()

        assertEquals(listOf(entity.toDomain()), result)
    }

    // ------------------------------------------------------------------
    // save
    // ------------------------------------------------------------------

    @Test
    fun `deve salvar o registro localmente e no firebase usando a data como id`() = runTest {
        coEvery { dao.insert(any()) } returns Unit
        val document = remote.document(documentId)
        every { document.set(any()) } returns Tasks.forResult(null)

        repository.save(record)

        coVerify(exactly = 1) { dao.insert(record.toEntity()) }
        verify(exactly = 1) { document.set(record.toDto()) }
    }

    @Test
    fun `deve manter o dado local mesmo se o firebase falhar ao salvar`() = runTest {
        coEvery { dao.insert(any()) } returns Unit
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.set(any()) } returns failedTask()

        repository.save(record)

        coVerify(exactly = 1) { dao.insert(record.toEntity()) }
    }

    @Test
    fun `save sem usuario logado nao deve tocar no firebase`() = runTest {
        givenNoLoggedUser()
        coEvery { dao.insert(any()) } returns Unit

        repository.save(record)

        coVerify(exactly = 1) { dao.insert(any()) }
        verify(exactly = 0) { firestore.collection(any()) }
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Test
    fun `deve remover o registro localmente e no firebase`() = runTest {
        coEvery { dao.delete(any()) } returns Unit
        val document = remote.document(documentId)
        every { document.delete() } returns Tasks.forResult(null)

        repository.delete(record)

        coVerify(exactly = 1) { dao.delete(record.toEntity()) }
        verify(exactly = 1) { document.delete() }
    }

    @Test
    fun `deve manter a remocao local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.delete(any()) } returns Unit
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.delete() } returns failedTask()

        repository.delete(record)

        coVerify(exactly = 1) { dao.delete(any()) }
    }

    // ------------------------------------------------------------------
    // deleteAll
    // ------------------------------------------------------------------

    @Test
    fun `deleteAll deve limpar o banco e cada documento do firebase`() = runTest {
        coEvery { dao.deleteAll() } returns Unit

        val doc1 = documentSnapshot("2026-01-01")
        val doc2 = documentSnapshot("2026-02-01")

        // Mock for the DocumentReference of each snapshot
        val ref1 = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        val ref2 = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)

        every { doc1.reference } returns ref1
        every { doc2.reference } returns ref2

        every { ref1.delete() } returns okTask()
        every { ref2.delete() } returns okTask()

        every { remote.collection.get() } returns Tasks.forResult(querySnapshot(doc1, doc2))

        repository.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
        verify { ref1.delete() }
        verify { ref2.delete() }
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
}
