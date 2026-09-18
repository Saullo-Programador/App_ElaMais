package com.example.ela.domain.usecase.care

import androidx.credentials.provider.Action
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.repository.CareActionRepository
import javax.inject.Inject

class DeleteCareActionsUseCase @Inject constructor(
    private val repository: CareActionRepository
) {
    suspend operator fun invoke (id: Long){
        repository.deleteById(id)
    }
}