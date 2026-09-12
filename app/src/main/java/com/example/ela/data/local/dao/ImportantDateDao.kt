package com.example.ela.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ela.data.local.entity.ImportantDateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportantDateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(date: ImportantDateEntity)

    @Query("SELECT * FROM important_dates ORDER BY date ASC")
    fun getAll(): Flow<List<ImportantDateEntity>>

    @Delete
    suspend fun delete(date: ImportantDateEntity)

    @Query("DELETE FROM important_dates")
    suspend fun deleteAll()
}