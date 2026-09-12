package com.example.ela.data.repository

import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.CycleDto
import com.example.ela.domain.model.Cycle
import com.example.ela.domain.repository.CycleRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CycleRepositoryImpl (
    private val dao: CycleDao,
    private val firestore: FirebaseFirestore
): CycleRepository {

    private val collection = firestore.collection("cycles")

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
            collection.document("user_cycle")
                .set(cycle.toDto())
                .await()
        }catch (e: Exception){
            // falhou? tudo bem, já salvou local
        }
    }

    override suspend fun syncCycle() {
        try {
            val snapshot = collection.document("user_cycle").get().await()
            val dto = snapshot.toObject(CycleDto::class.java)

            dto?.let {
                dao.insertCycle(it.toDomain().toEntity())
            }
        }catch (e: Exception){
            // sem internet -> ignora
        }
    }

}