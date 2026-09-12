package com.example.ela.domain.usecase.cycle

import com.example.ela.domain.model.Cycle
import com.example.ela.domain.repository.CycleRepository
import javax.inject.Inject

class SaveCycleUseCase @Inject constructor(
    private val repository: CycleRepository
) {

    suspend operator fun invoke(cycle: Cycle) {
        require(cycle.cycleLength in 20..40) {
            "Ciclo inválido"
        }

        require(cycle.periodLength in 2..10) {
            "Duração da menstruação inválida"
        }

        repository.saveCycle(cycle)
    }
}