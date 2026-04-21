package com.example.ela.domain.repository

import com.example.ela.domain.model.ImportantDate
import kotlinx.coroutines.flow.Flow

interface ImportantDateRepository {

    fun getDates(): Flow<List<ImportantDate>>

    suspend fun saveDate(date: ImportantDate)

    suspend fun deleteDate(date: ImportantDate)

    suspend fun syncDates()

}