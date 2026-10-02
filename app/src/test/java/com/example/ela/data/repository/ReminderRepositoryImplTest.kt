package com.example.ela.data.repository

import com.example.ela.data.local.dao.ReminderDao
import com.example.ela.data.local.entity.ReminderEntity
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.ReminderDto
import com.example.ela.domain.model.Reminder
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

class ReminderRepositoryImplTest {

    private lateinit var dao: ReminderDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var authRepository: AuthRepository
    private lateinit var remote: FirestoreUserCollectionMock
    private lateinit var repository: ReminderRepositoryImpl

    private val reminder = Reminder(id = 0, title = "Remédio", description = "Manhã", date = 1_000L, type = "Medicação")

    @Before
    fun setup() {
        dao = mockk()
        firestore = mockk()
        authRepository = mockk()

        every { authRepository.getCurrentUser() } returns User(uid = "uid-1")
        remote = FirestoreUserCollectionMock(firestore, "uid-1", "reminders")

        repository = ReminderRepositoryImpl(dao, firestore, authRepository)
    }

    private fun givenNoLoggedUser() {
        every { authRepository.getCurrentUser() } returns null
    }

    @Test
    fun `getReminder deve retornar os lembretes mapeados do banco`() = runTest {
        val entity = ReminderEntity(id = 1, title = "Remédio", description = "Manhã", date = 1_000L, type = "Medicação")
        every { dao.getAll() } returns flowOf(listOf(entity))

        assertEquals(listOf(entity.toDomain()), repository.getReminder().first())
    }

    @Test
    fun `saveReminder deve usar o id gerado pelo banco ao enviar para o firebase`() = runTest {
        coEvery { dao.insert(any()) } returns 42L
        val document = remote.document("42")
        every { document.set(any()) } returns Tasks.forResult(null)

        repository.saveReminder(reminder)

        coVerify(exactly = 1) { dao.insert(reminder.toEntity()) }
        verify(exactly = 1) { document.set(reminder.copy(id = 42L).toDto()) }
    }

    @Test
    fun `saveReminder deve manter o dado local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.insert(any()) } returns 42L
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.set(any()) } returns failedTask()

        repository.saveReminder(reminder)

        coVerify(exactly = 1) { dao.insert(any()) }
    }

    @Test
    fun `saveReminder sem usuario logado nao deve tocar no firebase`() = runTest {
        givenNoLoggedUser()
        coEvery { dao.insert(any()) } returns 1L

        repository.saveReminder(reminder)

        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `deleteReminder deve remover localmente e no firebase pelo id original`() = runTest {
        val saved = reminder.copy(id = 7)
        coEvery { dao.delete(any()) } returns Unit
        val document = remote.document("7")
        every { document.delete() } returns Tasks.forResult(null)

        repository.deleteReminder(saved)

        coVerify(exactly = 1) { dao.delete(saved.toEntity()) }
        verify(exactly = 1) { document.delete() }
    }

    @Test
    fun `deleteReminder deve manter a remocao local mesmo se o firebase falhar`() = runTest {
        val saved = reminder.copy(id = 7)
        coEvery { dao.delete(any()) } returns Unit
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.delete() } returns failedTask()

        repository.deleteReminder(saved)

        coVerify(exactly = 1) { dao.delete(any()) }
    }

    @Test
    fun `syncReminder deve gravar no banco local cada lembrete vindo do firebase`() = runTest {
        val dto = ReminderDto("7", "Remédio", "Manhã", 1_000L, "Medicação")
        val doc = documentSnapshot("7")
        every { doc.toObject(ReminderDto::class.java) } returns dto
        every { remote.collection.get() } returns Tasks.forResult(querySnapshot(doc))
        coEvery { dao.insert(any()) } returns 1L

        repository.syncReminder()

        coVerify(exactly = 1) { dao.insert(dto.toDomain().toEntity()) }
    }

    @Test
    fun `syncReminder deve ignorar documentos invalidos`() = runTest {
        val brokenDoc = documentSnapshot("x")
        every { brokenDoc.toObject(ReminderDto::class.java) } returns null
        every { remote.collection.get() } returns Tasks.forResult(querySnapshot(brokenDoc))

        repository.syncReminder()

        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `syncReminder nao deve quebrar quando o firebase falha`() = runTest {
        every { remote.collection.get() } returns failedTaskGeneric<com.google.firebase.firestore.QuerySnapshot>()

        repository.syncReminder()

        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `deleteAll deve limpar o banco e cada documento do firebase`() = runTest {
        coEvery { dao.deleteAll() } returns Unit
        every { remote.collection.get() } returns Tasks.forResult(querySnapshot(documentSnapshot("1"), documentSnapshot("2")))
        val docMock = mockk<com.google.firebase.firestore.DocumentReference>(relaxed = true)
        every { remote.collection.document(any()) } returns docMock
        every { docMock.delete() } returns okTask()

        repository.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
        verify(atLeast = 1) { docMock.delete() }
    }

    @Test
    fun `deleteAll deve limpar o banco local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.deleteAll() } returns Unit
        every { remote.collection.get() } returns failedTaskGeneric<com.google.firebase.firestore.QuerySnapshot>()

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
