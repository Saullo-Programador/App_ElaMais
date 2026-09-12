package com.example.ela.domain.usecase.cycle_record

import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.repository.CycleRecordRepository
import javax.inject.Inject

class DeleteCycleRecordUseCase @Inject constructor(
    private val repository: CycleRecordRepository
) {

    suspend operator fun invoke(record: CycleRecord) {
        repository.delete(record)
    }
}