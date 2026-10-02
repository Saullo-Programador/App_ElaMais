package com.example.ela.data.repository

import com.example.ela.data.local.dao.PreferencesDao
import com.example.ela.data.local.entity.PreferencesEntity
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.PreferencesDto
import com.example.ela.domain.model.Preferences
import com.example.ela.domain.model.User
import com.example.ela.domain.repository.AuthRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
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
import org.junit.Before
import org.junit.Test

class PreferencesRepositoryImplTest {

    private lateinit var dao: PreferencesDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var authRepository: AuthRepository
    private lateinit var document: DocumentReference
    private lateinit var repository: PreferencesRepositoryImpl

    private val preferences = Preferences(id = 1, notificationsTime = "09:00", isDarkMode = true)

    @Before
    fun setup() {
        dao = mockk()
        firestore = mockk()
        authRepository = mockk()

        every { authRepository.getCurrentUser() } returns User(uid = "uid-1")

        // users/{uid}/preferences/settings  (documento único, sem lista de documentos)
        val users = mockk<com.google.firebase.firestore.CollectionReference>()
        val userDoc = mockk<DocumentReference>()
        val preferencesCollection = mockk<com.google.firebase.firestore.CollectionReference>()
        document = mockk()
        every { firestore.collection("users") } returns users
        every { users.document("uid-1") } returns userDoc
        every { userDoc.collection("preferences") } returns preferencesCollection
        every { preferencesCollection.document("settings") } returns document

        repository = PreferencesRepositoryImpl(dao, firestore, authRepository)
    }

    private fun givenNoLoggedUser() {
        every { authRepository.getCurrentUser() } returns null
    }

    @Test
    fun `getPreferences deve retornar as preferencias mapeadas do banco`() = runTest {
        val entity = PreferencesEntity(id = 1, favoriteFoods = "[]", favoriteSweets = "[]", dislikedThings = "[]", symptoms = "[]")
        every { dao.get() } returns flowOf(entity)

        assertEquals(entity.toDomain(), repository.getPreferences().first())
    }

    @Test
    fun `getPreferences deve retornar null quando nao ha preferencias salvas`() = runTest {
        every { dao.get() } returns flowOf(null)

        assertEquals(null, repository.getPreferences().first())
    }

    @Test
    fun `savePreferences deve salvar localmente e no firebase`() = runTest {
        coEvery { dao.save(any()) } returns Unit
        every { document.set(any()) } returns Tasks.forResult(null)

        repository.savePreferences(preferences)

        coVerify(exactly = 1) { dao.save(preferences.toEntity()) }
        verify(exactly = 1) { document.set(preferences.toDto()) }
    }

    @Test
    fun `savePreferences deve manter o dado local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.save(any()) } returns Unit
        every { document.set(any()) } throws RuntimeException("Firebase Error")

        repository.savePreferences(preferences)

        coVerify(exactly = 1) { dao.save(any()) }
    }

    @Test
    fun `savePreferences sem usuario logado nao deve tocar no firebase`() = runTest {
        givenNoLoggedUser()
        coEvery { dao.save(any()) } returns Unit

        repository.savePreferences(preferences)

        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `syncPreferences deve gravar no banco local as preferencias vindas do firebase`() = runTest {
        val dto = PreferencesDto(id = "1", notificationsTime = "09:00", isDarkMode = true)
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(PreferencesDto::class.java) } returns dto
        every { document.get() } returns Tasks.forResult(snapshot)
        coEvery { dao.save(any()) } returns Unit

        repository.syncPreferences()

        coVerify(exactly = 1) { dao.save(dto.toDomain().toEntity()) }
    }

    @Test
    fun `syncPreferences nao deve salvar nada quando nao ha documento no firebase`() = runTest {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.toObject(PreferencesDto::class.java) } returns null
        every { document.get() } returns Tasks.forResult(snapshot)

        repository.syncPreferences()

        coVerify(exactly = 0) { dao.save(any()) }
    }

    @Test
    fun `syncPreferences nao deve quebrar quando o firebase falha`() = runTest {
        every { document.get() } throws RuntimeException("Firebase Error")

        repository.syncPreferences()

        coVerify(exactly = 0) { dao.save(any()) }
    }

    @Test
    fun `updateDarkMode deve salvar as preferencias existentes com o novo valor`() = runTest {
        val current = PreferencesEntity(id = 1, favoriteFoods = "[]", favoriteSweets = "[]", dislikedThings = "[]", symptoms = "[]", isDarkMode = false)
        every { dao.get() } returns flowOf(current)
        coEvery { dao.save(any()) } returns Unit
        every { document.set(any()) } returns Tasks.forResult(null)

        repository.updateDarkMode(true)

        coVerify(exactly = 1) {
            dao.save(match { it.isDarkMode })
        }
    }

    @Test
    fun `updateDarkMode sem preferencias salvas deve criar com os valores padrao`() = runTest {
        every { dao.get() } returns flowOf(null)
        coEvery { dao.save(any()) } returns Unit
        every { document.set(any()) } returns Tasks.forResult(null)

        repository.updateDarkMode(true)

        coVerify(exactly = 1) {
            dao.save(match { it.isDarkMode && it.timesPerDay == 1 })
        }
    }

    @Test
    fun `deleteAll deve limpar o banco local e o documento no firebase`() = runTest {
        coEvery { dao.deleteAll() } returns Unit
        every { document.delete() } returns Tasks.forResult(null)

        repository.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
        verify(exactly = 1) { document.delete() }
    }

    @Test
    fun `deleteAll deve limpar o banco local mesmo se o firebase falhar`() = runTest {
        coEvery { dao.deleteAll() } returns Unit
        every { document.delete() } throws RuntimeException("Firebase Error")

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
