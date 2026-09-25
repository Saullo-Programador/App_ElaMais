package com.example.ela.domain.repository

import com.example.ela.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signup(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): User?
}