package com.example.ela.domain.repository

import com.example.ela.domain.model.Cycle
import kotlinx.coroutines.flow.Flow

interface CycleRepository {
    fun getCycle(): Flow<Cycle?>

    suspend fun saveCycle(cycle: Cycle)

    suspend fun syncCycle()
}