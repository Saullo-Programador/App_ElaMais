package com.example.ela.domain.repository

import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import kotlinx.coroutines.flow.Flow

interface CareActionRepository {

    fun getByPhase(phase: CyclePhase): Flow<List<CareAction>>

    suspend fun update(action: CareAction)

    suspend fun save(action: CareAction)
}