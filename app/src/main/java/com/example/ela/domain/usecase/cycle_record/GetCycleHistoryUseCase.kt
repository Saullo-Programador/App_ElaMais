package com.example.ela.domain.usecase.cycle_record

import com.example.ela.domain.repository.CycleRecordRepository
import javax.inject.Inject

class GetCycleHistoryUseCase @Inject constructor(
    private val repository: CycleRecordRepository
) {

    operator fun invoke() = repository.getHistory()
}