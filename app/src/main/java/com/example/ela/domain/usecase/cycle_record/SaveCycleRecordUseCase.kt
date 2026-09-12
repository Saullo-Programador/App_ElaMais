package com.example.ela.domain.usecase.cycle_record

import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.repository.CycleRecordRepository
import javax.inject.Inject

class SaveCycleRecordUseCase @Inject constructor(
    private val repository: CycleRecordRepository
) {

    suspend operator fun invoke(start: Long, end: Long = 0) {

        if (end > 0) {
            require(end >= start) {
                "Data inválida"
            }
        }

        repository.save(
            CycleRecord(
                id = 0,
                startDate = start,
                endDate = end
            )
        )
    }
}