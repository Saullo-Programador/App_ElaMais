package com.example.ela.domain.usecase.cycle

import com.example.ela.domain.repository.CycleRepository
import javax.inject.Inject

class GetCycleUseCase @Inject constructor(
    private val repository: CycleRepository
) {

    operator fun invoke() = repository.getCycle()
}