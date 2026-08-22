package com.example.ela.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ela.data.local.entity.PreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferencesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(preferences: PreferencesEntity)

    @Query("SELECT * FROM preferences LIMIT 1")
    fun get(): Flow<PreferencesEntity?>

    @Query("DELETE FROM preferences")
    suspend fun deleteAll()

    @Query("UPDATE preferences SET isDarkMode = :isDarkMode")
    suspend fun updateDarkMode(
        isDarkMode: Boolean
    )

}