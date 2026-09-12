package com.example.ela.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ela.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Query("SELECT * FROM reminder ORDER BY date ASC")
    fun getAll(): Flow<List<ReminderEntity>>

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("DELETE FROM reminder")
    suspend fun deleteAll()
}