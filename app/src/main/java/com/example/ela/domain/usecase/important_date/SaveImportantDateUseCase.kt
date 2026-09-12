package com.example.ela.domain.usecase.important_date

import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.repository.ImportantDateRepository
import javax.inject.Inject

class SaveImportantDateUseCase @Inject constructor(
    private val repository: ImportantDateRepository
) {

    suspend operator fun invoke(date: ImportantDate) {
        repository.saveDate(date)
    }
}