package com.example.ela.domain.repository

import com.example.ela.domain.model.CycleRecord
import kotlinx.coroutines.flow.Flow

interface CycleRecordRepository {

    fun getHistory(): Flow<List<CycleRecord>>

    suspend fun save(record: CycleRecord)

    suspend fun delete(record: CycleRecord)
}