package com.example.ela.domain.usecase.auth

import com.example.ela.domain.model.User
import com.example.ela.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): User? {
        return repository.getCurrentUser()
    }
}