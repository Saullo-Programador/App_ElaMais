package com.example.ela.data.repository

import app.cash.turbine.test
import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.local.entity.CycleEntity
import com.example.ela.data.remote.dto.CycleDto
import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.User
import com.example.ela.domain.repository.AuthRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
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
import kotlin.test.assertNotNull

class CycleRepositoryImplTest {

    private lateinit var dao: CycleDao
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var repository: CycleRepositoryImpl
    private lateinit var authRepository: AuthRepository

    private val testUid = "uid_teste"

    @Before
    fun setup() {
        dao = mockk()
        firebaseFirestore = mockk()
        authRepository = mockk()

        every { authRepository.getCurrentUser() } returns User(uid = testUid)

        repository = CycleRepositoryImpl(
            dao,
            firebaseFirestore,
            authRepository
        )
    }

    /**
     * Monta o caminho real usado pelo repositório:
     * users/{uid}/cycles/current
     */
    private fun mockCycleDocument(): DocumentReference {
        val users = mockk<CollectionReference>()
        val userDoc = mockk<DocumentReference>()
        val cycles = mockk<CollectionReference>()
        val cycleDoc = mockk<DocumentReference>()

        every { firebaseFirestore.collection("users") } returns users
        every { users.document(testUid) } returns userDoc
        every { userDoc.collection("cycles") } returns cycles
        every { cycles.document("current") } returns cycleDoc

        return cycleDoc
    }

    @Test
    fun `deve mapear cycle entity para domain`() = runTest {

        val entity = CycleEntity(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = 1725148800000L
        )

        every {
            dao.getCycle()
        } returns flowOf(entity)

        repository.getCycle().test {

            val result = awaitItem()

            assertNotNull(result)

            assertEquals(entity.id, result.id)
            assertEquals(entity.cycleLength, result.cycleLength)
            assertEquals(entity.periodLength, result.periodLength)
            assertEquals(entity.lastPeriodStart, result.lastPeriodStart)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deve retornar null quando nao existir ciclo`() = runTest {

        every {
            dao.getCycle()
        } returns flowOf(null)

        repository.getCycle().test {

            val result = awaitItem()

            assertEquals(null, result)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deve salvar ciclo localmente e tentar salvar no firebase`() = runTest {

        val cycle = Cycle(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = 1725148800000L
        )

        val document = mockCycleDocument()

        // Task real já concluído
        every {
            document.set(any())
        } returns Tasks.forResult(null)

        coEvery {
            dao.insertCycle(any())
        } returns Unit

        repository.saveCycle(cycle)

        coVerify {
            dao.insertCycle(any())
        }

        verify {
            document.set(any())
        }
    }

    @Test
    fun `deve salvar localmente mesmo se firebase falhar`() = runTest {

        val cycle = Cycle(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = 1725148800000L
        )

        val document = mockCycleDocument()

        every {
            document.set(any())
        } throws RuntimeException("Firebase Error")

        coEvery {
            dao.insertCycle(any())
        } returns Unit

        repository.saveCycle(cycle)

        coVerify {
            dao.insertCycle(any())
        }
    }

    @Test
    fun `deve sincronizar ciclo do firebase para o banco local`() = runTest {

        val mockDto = CycleDto(
            id = 1,
            cycleLength = 30,
            periodLength = 6,
            lastPeriodStart = 1725148800000L
        )

        val mockSnapshot = mockk<DocumentSnapshot>()

        every {
            mockSnapshot.toObject(CycleDto::class.java)
        } returns mockDto

        val document = mockCycleDocument()

        // Task real concluído
        every {
            document.get()
        } returns Tasks.forResult(mockSnapshot)

        coEvery {
            dao.insertCycle(any())
        } returns Unit

        repository.syncCycle()

        coVerify {
            dao.insertCycle(
                match {
                    it.id.toInt() == 1 &&
                            it.cycleLength == 30 &&
                            it.periodLength == 6 &&
                            it.lastPeriodStart == 1725148800000L
                }
            )
        }
    }
}