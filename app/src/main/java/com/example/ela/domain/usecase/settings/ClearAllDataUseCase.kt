package com.example.ela.domain.usecase.settings

import com.example.ela.domain.repository.CareActionRepository
import com.example.ela.domain.repository.CycleRepository
import com.example.ela.domain.repository.CycleRecordRepository
import com.example.ela.domain.repository.ImportantDateRepository
import com.example.ela.domain.repository.PreferencesRepository
import com.example.ela.domain.repository.ReminderRepository
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val careActionRepository: CareActionRepository,
    private val importantDateRepository: ImportantDateRepository,
    private val reminderRepository: ReminderRepository,
    private val cycleRecordRepository: CycleRecordRepository,
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke() {
        cycleRepository.deleteAll()
        careActionRepository.deleteAll()
        importantDateRepository.deleteAll()
        reminderRepository.deleteAll()
        cycleRecordRepository.deleteAll()
        preferencesRepository.deleteAll()
    }
}
