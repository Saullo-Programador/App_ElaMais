package com.example.ela.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ela.data.local.entity.CycleRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CycleRecordEntity)

    @Query("SELECT * FROM cycle_records ORDER BY startDate ASC")
    fun getAll(): Flow<List<CycleRecordEntity>>

    @Delete
    suspend fun delete(record: CycleRecordEntity)

    @Query("DELETE FROM cycle_records")
    suspend fun deleteAll()
}