package com.example.ela.data.repository

import app.cash.turbine.test
import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.local.entity.CycleEntity
import com.example.ela.domain.model.Cycle
import com.google.firebase.firestore.FirebaseFirestore
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

    @Before
    fun setup() {
        dao = mockk()

        firebaseFirestore = mockk()

        every {
            firebaseFirestore.collection("cycles")
        } returns mockk(relaxed = true)

        repository = CycleRepositoryImpl(
            dao,
            firebaseFirestore
        )
    }

    @Test
    fun `deve mapear cycle entity para domain`() = runTest {

        // Arrange
        val entity = CycleEntity(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = 1725148800000L
        )

        every {
            dao.getCycle()
        } returns flowOf(entity)

        // Act
        repository.getCycle()
            .test {

                val result = awaitItem()

                // Assert
                assertNotNull(result)

                assertEquals(
                    entity.id,
                    result.id
                )

                assertEquals(
                    entity.cycleLength,
                    result.cycleLength
                )

                assertEquals(
                    entity.periodLength,
                    result.periodLength
                )

                assertEquals(
                    entity.lastPeriodStart,
                    result.lastPeriodStart
                )

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `deve retornar null quando nao existir ciclo`() = runTest {

        // Arrange
        every {
            dao.getCycle()
        } returns flowOf(null)

        // Act
        repository.getCycle()
            .test {

                val result = awaitItem()

                // Assert
                assertEquals(
                    null,
                    result
                )

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `deve salvar ciclo localmente e tentar salvar no firebase`() = runTest {
        // Arrange
        val cycle = Cycle(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = 1725148800000L
        )

        every { dao.insertCycle(any()) } returns Unit
        // O mock relaxed do firestore cuidará do .set().await()

        // Act
        repository.saveCycle(cycle)

        // Assert
        verify { dao.insertCycle(any()) }
    }

        @Test
        fun `deve salvar localmente mesmo se firebase falhar`() = runTest {
            // Arrange
            val cycle = Cycle(1, 28, 5, 1725148800000L)

            every { dao.insertCycle(any()) } returns Unit

            // Forçamos o firestore a lançar exceção no collection ou document
            every { firebaseFirestore.collection("cycles") } throws RuntimeException("Firebase Error")

            // Act
            repository.saveCycle(cycle)

            // Assert
            verify { dao.insertCycle(any()) } // Deve ter salvado localmente
        }

        @Test
        fun `deve sincronizar ciclo do firebase para o banco local`() = runTest {
            // Arrange
            val mockDto = com.example.ela.data.remote.dto.CycleDto(
                id = 1,
                cycleLength = 30,
                periodLength = 6,
                lastPeriodStart = 1725148800000L
            )

            // Mock complexo para simular o Task do Firebase
            val mockDocument = mockk<com.google.firebase.firestore.DocumentSnapshot>()
            every { mockDocument.toObject(com.example.ela.data.remote.dto.CycleDto::class.java) } returns mockDto

            val mockTask =
                mockk<com.google.android.gms.tasks.Task<com.google.firebase.firestore.DocumentSnapshot>>()
            every { mockTask.isComplete } returns true
            every { mockTask.isSuccessful } returns true
            every { mockTask.getResult } returns mockDocument

            val docRef = mockk<com.google.firebase.firestore.DocumentReference>()
            every { docRef.get() } returns mockTask

            val collRef = mockk<com.google.firebase.firestore.CollectionReference>()
            every { collRef.document("user_cycle") } returns docRef

            every { firebaseFirestore.collection("cycles") } returns collRef
            every { dao.insertCycle(any()) } returns Unit

            // Act
            repository.syncCycle()

            // Assert
            verify { dao.insertCycle(any()) }
        }
    }
