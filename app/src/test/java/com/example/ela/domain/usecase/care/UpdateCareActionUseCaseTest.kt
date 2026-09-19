package com.example.ela.domain.usecase.care

import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.repository.CareActionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UpdateCareActionUseCaseTest {

    private val repository: CareActionRepository = mockk()
    private val useCase = UpdateCareActionUseCase(repository)

    @Test
    fun `should update care action via repository`() = runBlocking {
        val action = CareAction(1, "Title", "Desc", CyclePhase.MENSTRUAL, true)
        coEvery { repository.update(any()) } returns Unit

        useCase(action)

        coVerify { repository.update(action) }
    }
}
