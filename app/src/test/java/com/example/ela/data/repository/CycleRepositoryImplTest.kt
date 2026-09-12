package com.example.ela.data.repository

import app.cash.turbine.test
import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.local.entity.CycleEntity
import com.example.ela.data.remote.dto.CycleDto
import com.example.ela.domain.model.Cycle
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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

    @Before
    fun setup() {
        dao = mockk()
        firebaseFirestore = mockk()

        val collection = mockk<CollectionReference>()

        every {
            firebaseFirestore.collection("cycles")
        } returns collection

        repository = CycleRepositoryImpl(
            dao,
            firebaseFirestore
        )
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

        val collection = mockk<CollectionReference>()
        val document = mockk<DocumentReference>()

        every {
            firebaseFirestore.collection("cycles")
        } returns collection

        every {
            collection.document("user_cycle")
        } returns document

        // Task real já concluído
        every {
            document.set(any())
        } returns Tasks.forResult(null)

        coEvery {
            dao.insertCycle(any())
        } returns Unit

        repository = CycleRepositoryImpl(
            dao,
            firebaseFirestore
        )

        repository.saveCycle(cycle)

        coVerify {
            dao.insertCycle(any())
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

        val collection = mockk<CollectionReference>()
        val document = mockk<DocumentReference>()

        every {
            firebaseFirestore.collection("cycles")
        } returns collection

        every {
            collection.document("user_cycle")
        } returns document

        every {
            document.set(any())
        } throws RuntimeException("Firebase Error")

        coEvery {
            dao.insertCycle(any())
        } returns Unit

        repository = CycleRepositoryImpl(
            dao,
            firebaseFirestore
        )

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

        val mockDocument = mockk<DocumentSnapshot>()

        every {
            mockDocument.toObject(CycleDto::class.java)
        } returns mockDto

        val documentReference = mockk<DocumentReference>()

        val collection = mockk<CollectionReference>()

        every {
            firebaseFirestore.collection("cycles")
        } returns collection

        every {
            collection.document("user_cycle")
        } returns documentReference

        // Task real concluído
        every {
            documentReference.get()
        } returns Tasks.forResult(mockDocument)

        coEvery {
            dao.insertCycle(any())
        } returns Unit

        repository = CycleRepositoryImpl(
            dao,
            firebaseFirestore
        )

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