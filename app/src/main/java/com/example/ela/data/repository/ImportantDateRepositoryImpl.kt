package com.example.ela.data.repository

import com.example.ela.data.local.dao.ImportantDateDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.ImportantDateDto
import com.example.ela.domain.model.ImportantDate
import com.example.ela.domain.repository.ImportantDateRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ImportantDateRepositoryImpl(
    private val dao: ImportantDateDao,
    private val firestore: FirebaseFirestore
) : ImportantDateRepository {

    private val collection = firestore.collection("important_dates")

    override fun getDates(): Flow<List<ImportantDate>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveDate(date: ImportantDate) {
        dao.insert(date.toEntity())

        try {
            collection.document(date.id.toString())
                .set(date.toDto())
                .await()
        } catch (e: Exception) {
            // falhou? tudo bem, já salvou local
        }
    }

    override suspend fun deleteDate(date: ImportantDate) {
        dao.delete(date.toEntity())

        try {
            collection.document(date.id.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            // falhou? tudo bem, já salvou local
        }
    }

    override suspend fun syncDates() {
        try {
            val snapshot = collection.get().await()

            val list = snapshot.documents.mapNotNull {
                it.toObject(ImportantDateDto::class.java)
            }

            list.forEach {
                dao.insert(it.toDomain().toEntity())
            }
        } catch (e: Exception) {
            // sem internet -> ignora
        }
    }
}