package com.example.ela.domain.usecase.auth

import com.example.ela.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.logout()
    }
}