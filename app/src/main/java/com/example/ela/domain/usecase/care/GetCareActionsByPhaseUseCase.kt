package com.example.ela.domain.usecase.care

import com.example.ela.domain.model.CyclePhase
import com.example.ela.domain.repository.CareActionRepository
import javax.inject.Inject

class GetCareActionsByPhaseUseCase @Inject constructor(
    private val repository: CareActionRepository
) {

    operator fun invoke(phase: CyclePhase) =
        repository.getByPhase(phase)
}