package com.example.ela.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ela.data.local.entity.CareActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareActionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: CareActionEntity)

    @Query("SELECT * FROM care_actions WHERE phase = :phase")
    fun getByPhase(phase: String): Flow<List<CareActionEntity>>

    @Update
    suspend fun update(action: CareActionEntity)

    @Query("DELETE FROM care_actions")
    suspend fun deleteAll()
}