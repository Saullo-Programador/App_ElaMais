package com.example.ela.domain.usecase.care

import com.example.ela.domain.repository.CareActionRepository
import javax.inject.Inject

class SyncCareActionsUseCase @Inject constructor(
    private val repository: CareActionRepository
) {
    suspend operator fun invoke() {
        repository.syncCareActions()
    }
}
