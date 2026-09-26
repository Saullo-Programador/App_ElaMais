package com.example.ela.data.repository

import com.example.ela.domain.model.Couple
import com.example.ela.domain.model.Invite
import com.example.ela.domain.model.InviteStatus
import com.example.ela.domain.model.SharingSettings
import com.example.ela.domain.model.User
import com.example.ela.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        return try {
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser: FirebaseUser =
                authResult.user
                    ?: return Result.failure(
                        Exception("Firebase não retornou o usuário após o login")
                    )

            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName
            )

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signup(
        email: String,
        password: String
    ): Result<User> {
        return try {
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser: FirebaseUser =
                authResult.user
                    ?: return Result.failure(
                        Exception("Firebase não retornou o usuário após o cadastro")
                    )

            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName
            )

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Firebase não retornou o usuário após o Google Login"))

            val user = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null

        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName
        )
    }

    // --- Couple/Invite Implementation ---

    override suspend fun sendInvite(invite: Invite): Result<Unit> {
        return try {
            val inviteId = firestore.collection("invites").document().id
            val inviteWithId = invite.copy(inviteId = inviteId)
            firestore.collection("invites").document(inviteId).set(inviteWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingInvites(email: String): Result<List<Invite>> {
        return try {
            val snapshot = firestore.collection("invites")
                .whereEqualTo("receiverEmail", email)
                .whereEqualTo("status", InviteStatus.PENDING.name)
                .get()
                .await()

            val invites = snapshot.documents.mapNotNull { it.toObject(Invite::class.java) }
            Result.success(invites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptInvite(inviteId: String, receiverId: String): Result<Couple> {
        return try {
            val inviteDoc = firestore.collection("invites").document(inviteId).get().await()
            val invite = inviteDoc.toObject(Invite::class.java)
                ?: return Result.failure(Exception("Convite não encontrado"))

            val coupleId = firestore.collection("couples").document().id
            val couple = Couple(
                coupleId = coupleId,
                partner1Id = invite.senderId,
                partner2Id = receiverId
            )

            // 1. Criar o casal
            firestore.collection("couples").document(coupleId).set(couple).await()

            // 2. Atualizar o status do convite
            firestore.collection("invites").document(inviteId)
                .update("status", InviteStatus.ACCEPTED.name)
                .await()

            Result.success(couple)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCoupleByUserId(userId: String): Result<Couple?> {
        return try {
            val snapshot = firestore.collection("couples")
                .whereEqualTo("partner1Id", userId)
                .get()
                .await()

            var couple = snapshot.documents.firstOrNull()?.toObject(Couple::class.java)

            if (couple == null) {
                val snapshot2 = firestore.collection("couples")
                    .whereEqualTo("partner2Id", userId)
                    .get()
                    .await()
                couple = snapshot2.documents.firstOrNull()?.toObject(Couple::class.java)
            }

            Result.success(couple)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSharingSettings(
        coupleId: String,
        settings: SharingSettings
    ): Result<Unit> {
        return try {
            firestore.collection("couples").document(coupleId)
                .set(mapOf("sharingSettings" to settings), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
