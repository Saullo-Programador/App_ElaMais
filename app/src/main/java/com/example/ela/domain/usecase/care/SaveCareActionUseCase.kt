package com.example.ela.domain.usecase.care

import com.example.ela.domain.model.CareAction
import com.example.ela.domain.repository.CareActionRepository
import javax.inject.Inject

class SaveCareActionUseCase @Inject constructor(
    private val repository: CareActionRepository
) {
    suspend operator fun invoke(care: CareAction) {
        require(care.title.isNotBlank()) {
            "Título obrigatório"
        }
        repository.save(care)
    }

}