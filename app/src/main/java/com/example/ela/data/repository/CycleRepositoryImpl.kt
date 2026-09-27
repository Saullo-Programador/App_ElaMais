package com.example.ela.data.repository

import android.util.Log
import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.CycleDto
import com.example.ela.domain.model.Cycle
import com.example.ela.domain.repository.CycleRepository
import com.example.ela.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CycleRepositoryImpl (
    private val dao: CycleDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
): CycleRepository {

    private fun getCycleDocument() =
        authRepository.getCurrentUser()?.uid?.let { uid ->
            firestore
                .collection("users")
                .document(uid)
                .collection("cycles")
                .document("current")
        }

    override fun getCycle(): Flow<Cycle?> {
        return dao.getCycle().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveCycle(cycle: Cycle) {
        // 🔹 Salva local primeiro (offline-first)
        dao.insertCycle(cycle.toEntity())
        // 🔹 Tenta enviar pro Firebase
        try {
            getCycleDocument()?.set(cycle.toDto())?.await()
        } catch (e: Exception) {
            // Firebase indisponível.
            // Os dados continuam salvos localmente.
            Log.e(
                "CycleRepository",
                "Erro ao salvar o Cycle no Firebase",
                e
            )
        }
    }

    override suspend fun syncCycle() {
        try {
            val snapshot = getCycleDocument()?.get()?.await()
            val dto = snapshot?.toObject(CycleDto::class.java)

            dto?.let {
                dao.insertCycle(it.toDomain().toEntity())
            }
        } catch (e: Exception) {
            Log.e(
                "CycleRepository",
                "Erro ao sincronizar Cycle do Firebase",
                e
            )
        }
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
        try {
            getCycleDocument()?.delete()?.await()
        } catch (e: Exception) {
            Log.e(
                "CycleRepository",
                "Erro ao deletar Cycle no Firebase",
                e
            )
        }
    }
}
