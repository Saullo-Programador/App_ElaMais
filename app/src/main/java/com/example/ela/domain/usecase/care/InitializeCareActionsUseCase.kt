package com.example.ela.domain.usecase.care

import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.repository.CareActionRepository
import javax.inject.Inject

class InitializeCareActionsUseCase @Inject constructor(
    private val repository: CareActionRepository
) {
    suspend operator fun invoke(phase: CyclePhase) {
        repository.initializeDefaults(phase)
    }
}