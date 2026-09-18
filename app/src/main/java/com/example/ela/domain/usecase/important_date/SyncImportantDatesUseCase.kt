package com.example.ela.domain.usecase.important_date

import com.example.ela.domain.repository.ImportantDateRepository
import javax.inject.Inject

class SyncImportantDatesUseCase @Inject constructor(
    private val repository: ImportantDateRepository
) {
    suspend operator fun invoke() {
        repository.syncDates()
    }
}
