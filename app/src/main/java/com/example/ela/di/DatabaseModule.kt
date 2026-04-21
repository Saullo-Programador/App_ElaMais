package com.example.ela.di

import android.content.Context
import androidx.room.Room
import com.example.ela.data.local.dao.CareActionDao
import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.local.dao.CycleRecordDao
import com.example.ela.data.local.dao.ImportantDateDao
import com.example.ela.data.local.dao.PreferencesDao
import com.example.ela.data.local.dao.ReminderDao
import com.example.ela.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides fun provideCycleDao(db: AppDatabase): CycleDao = db.cycleDao()
    @Provides fun provideCareActionDao(db: AppDatabase): CareActionDao = db.careActionDao()
    @Provides fun provideImportantDateDao(db: AppDatabase): ImportantDateDao = db.importantDateDao()
    @Provides fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()
    @Provides fun providePreferencesDao(db: AppDatabase): PreferencesDao = db.preferencesDao()
    @Provides fun provideCycleRecordDao(db: AppDatabase): CycleRecordDao = db.cycleRecordDao()
}
