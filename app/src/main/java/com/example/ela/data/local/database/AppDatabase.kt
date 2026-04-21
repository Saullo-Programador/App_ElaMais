package com.example.ela.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ela.data.local.converter.Converters
import com.example.ela.data.local.dao.CareActionDao
import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.local.dao.CycleRecordDao
import com.example.ela.data.local.dao.ImportantDateDao
import com.example.ela.data.local.dao.PreferencesDao
import com.example.ela.data.local.dao.ReminderDao
import com.example.ela.data.local.entity.CareActionEntity
import com.example.ela.data.local.entity.CycleEntity
import com.example.ela.data.local.entity.CycleRecordEntity
import com.example.ela.data.local.entity.ImportantDateEntity
import com.example.ela.data.local.entity.PreferencesEntity
import com.example.ela.data.local.entity.ReminderEntity


@Database(
    entities = [
        CycleEntity::class,
        CareActionEntity::class,
        ImportantDateEntity::class,
        ReminderEntity::class,
        PreferencesEntity::class,
        CycleRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
    abstract fun careActionDao(): CareActionDao
    abstract fun importantDateDao(): ImportantDateDao
    abstract fun reminderDao(): ReminderDao
    abstract fun preferencesDao(): PreferencesDao
    abstract fun cycleRecordDao(): CycleRecordDao
}
