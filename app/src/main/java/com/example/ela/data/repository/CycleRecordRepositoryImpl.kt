package com.example.ela.data.repository

import com.example.ela.data.local.dao.CycleRecordDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toEntity
import com.example.ela.domain.model.CycleRecord
import com.example.ela.domain.repository.CycleRecordRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CycleRecordRepositoryImpl(
    private val dao: CycleRecordDao,
    private val firestore: FirebaseFirestore
) : CycleRecordRepository {

    override fun getHistory(): Flow<List<CycleRecord>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun save(record: CycleRecord) {
        dao.insert(record.toEntity())
    }

    override suspend fun delete(record: CycleRecord) {
        dao.delete(record.toEntity())
    }
}