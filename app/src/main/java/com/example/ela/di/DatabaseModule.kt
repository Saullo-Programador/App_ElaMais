package com.example.ela.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
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
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        SQLiteDatabase.loadLibs(context)
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs: SharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        var passphrase = prefs.getString("db_passphrase", null)
        if (passphrase == null) {
            passphrase = java.util.UUID.randomUUID().toString() + java.util.UUID.randomUUID().toString()
            prefs.edit().putString("db_passphrase", passphrase).apply()
        }
        val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).openHelperFactory(factory).build()
    }

    @Provides fun provideCycleDao(db: AppDatabase): CycleDao = db.cycleDao()
    @Provides fun provideCareActionDao(db: AppDatabase): CareActionDao = db.careActionDao()
    @Provides fun provideImportantDateDao(db: AppDatabase): ImportantDateDao = db.importantDateDao()
    @Provides fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()
    @Provides fun providePreferencesDao(db: AppDatabase): PreferencesDao = db.preferencesDao()
    @Provides fun provideCycleRecordDao(db: AppDatabase): CycleRecordDao = db.cycleRecordDao()
}