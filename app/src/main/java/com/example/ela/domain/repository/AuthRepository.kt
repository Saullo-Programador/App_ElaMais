package com.example.ela.domain.repository

import com.example.ela.domain.model.Couple
import com.example.ela.domain.model.Invite
import com.example.ela.domain.model.SharingSettings
import com.example.ela.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signup(email: String, password: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): User?

    // Couple/Invite methods
    suspend fun sendInvite(invite: Invite): Result<Unit>
    suspend fun getPendingInvites(email: String): Result<List<Invite>>
    suspend fun acceptInvite(inviteId: String, receiverId: String): Result<Couple>
    suspend fun getCoupleByUserId(userId: String): Result<Couple?>
    suspend fun updateSharingSettings(coupleId: String, settings: SharingSettings): Result<Unit>
}