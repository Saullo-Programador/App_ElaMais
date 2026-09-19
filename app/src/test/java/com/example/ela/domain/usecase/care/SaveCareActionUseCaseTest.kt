package com.example.ela.domain.usecase.care

import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.repository.CareActionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test

class SaveCareActionUseCaseTest {

    private val repository: CareActionRepository = mockk()
    private val useCase = SaveCareActionUseCase(repository)

    @Test
    fun `should save care action when title is not blank`() = runBlocking {
        val action = CareAction(1, "Title", "Desc", CyclePhase.MENSTRUAL, false)
        coEvery { repository.save(any()) } returns Unit

        useCase(action)

        coVerify { repository.save(action) }
    }

    @Test
    fun `should throw exception when title is blank`() {
        val action = CareAction(1, "", "Desc", CyclePhase.MENSTRUAL, false)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(action) }
        }
    }
}
