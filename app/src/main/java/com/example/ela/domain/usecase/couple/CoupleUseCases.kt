package com.example.ela.domain.usecase.couple

import com.example.ela.domain.model.Couple
import com.example.ela.domain.model.Invite
import com.example.ela.domain.model.SharingSettings
import com.example.ela.domain.repository.AuthRepository
import javax.inject.Inject

class SendInviteUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(receiverEmail: String): Result<Unit> {
        val currentUser = repository.getCurrentUser() ?: return Result.failure(Exception("Usuário não autenticado"))

        val invite = Invite(
            senderId = currentUser.uid,
            receiverEmail = receiverEmail
        )
        return repository.sendInvite(invite)
    }
}

class GetPendingInvitesUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<List<Invite>> {
        return repository.getPendingInvites(email)
    }
}

class AcceptInviteUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(inviteId: String): Result<Couple> {
        val currentUser = repository.getCurrentUser() ?: return Result.failure(Exception("Usuário não autenticado"))
        return repository.acceptInvite(inviteId, currentUser.uid)
    }
}

class GetCoupleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(userId: String): Result<Couple?> {
        return repository.getCoupleByUserId(userId)
    }
}

class UpdateSharingSettingsUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(coupleId: String, settings: SharingSettings): Result<Unit> {
        return repository.updateSharingSettings(coupleId, settings)
    }
}
