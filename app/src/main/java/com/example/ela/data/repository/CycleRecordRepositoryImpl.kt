package com.example.ela.data.repository

import android.util.Log
import com.example.ela.data.local.dao.CycleRecordDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.repository.AuthRepository
import com.example.ela.domain.repository.CycleRecordRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CycleRecordRepositoryImpl(
    private val dao: CycleRecordDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : CycleRecordRepository {


    private fun getCyclesCollection() =
        authRepository.getCurrentUser()?.uid?.let { uid ->
            firestore
                .collection("users")
                .document(uid)
                .collection("cycles")
        }

    private fun getDocumentId( startDate: Long ): String{
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        ).format(Date(startDate))
    }

    override fun getHistory(): Flow<List<CycleRecord>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun save(record: CycleRecord) {
        // 1. Salva localmente
        dao.insert(record.toEntity())

        // 2. Sincroniza com Firebase
        try {
            val documentId = getDocumentId(record.startDate)

            getCyclesCollection()
                ?.document(documentId)
                ?.set(record.toDto())
                ?.await()
        } catch (e: Exception){
            Log.e(
                "CycleRecordRepository",
                "Erro ao salvar histórico no Firebase",
                e
            )
        }
    }

    override suspend fun delete(record: CycleRecord) {
        // Remove localmente
        dao.delete(record.toEntity())

        // Remove do Firebase
        try {
            val documentId = getDocumentId(record.startDate)

            getCyclesCollection()
                ?.document(documentId)
                ?.delete()
                ?.await()
        } catch (e: Exception) {
            Log.e(
                "CycleRecordRepository",
                "Erro ao deletar histórico no Firebase",
                e
            )
        }
    }

    override suspend fun deleteAll() {
        // Remove local
        dao.deleteAll()

        // Remove Firebase

        try {
            val snapshot = getCyclesCollection()
                ?.get()
                ?.await()

            snapshot?.documents?.forEach { document ->
                document.reference.delete().await()
            }
        } catch (e: Exception){
            Log.e(
                "CycleRecordRepository",
                "Erro ao deletar histórico no Firebase",
                e
            )
        }
    }
}