package com.example.ela.domain.usecase.important_date

import com.example.ela.domain.repository.ImportantDateRepository
import javax.inject.Inject

class GetImportantDatesUseCase @Inject constructor(
    private val repository: ImportantDateRepository
) {
    operator fun invoke() = repository.getDates()
}