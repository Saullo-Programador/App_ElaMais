package com.example.ela.di

import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.local.dao.ImportantDateDao
import com.example.ela.data.local.dao.PreferencesDao
import com.example.ela.data.local.dao.ReminderDao
import com.example.ela.data.local.dao.CycleRecordDao
import com.example.ela.data.repository.CareActionRepositoryImpl
import com.example.ela.data.repository.CycleRepositoryImpl
import com.example.ela.data.repository.CycleRecordRepositoryImpl
import com.example.ela.data.repository.ImportantDateRepositoryImpl
import com.example.ela.data.repository.PreferencesRepositoryImpl
import com.example.ela.data.repository.ReminderRepositoryImpl
import com.example.ela.data.repository.AuthRepositoryImpl
import com.example.ela.domain.repository.CareActionRepository
import com.example.ela.domain.repository.CycleRepository
import com.example.ela.domain.repository.CycleRecordRepository
import com.example.ela.domain.repository.ImportantDateRepository
import com.example.ela.domain.repository.PreferencesRepository
import com.example.ela.domain.repository.ReminderRepository
import com.example.ela.domain.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository = impl

    @Provides
    fun provideCycleRepository(
        dao: CycleDao,
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): CycleRepository {
        return CycleRepositoryImpl(dao, firestore, authRepository)
    }

    @Provides
    fun provideReminderRepository(
        dao: ReminderDao,
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): ReminderRepository = ReminderRepositoryImpl(dao, firestore, authRepository)

    @Provides
    fun providePreferencesRepository(
        dao: PreferencesDao,
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): PreferencesRepository = PreferencesRepositoryImpl(dao, firestore, authRepository)

    @Provides
    fun provideImportantDateRepository(
        dao: ImportantDateDao,
        firestore: FirebaseFirestore
    ): ImportantDateRepository = ImportantDateRepositoryImpl(dao, firestore)

    @Provides
    fun provideCycleRecordRepository(
        dao: CycleRecordDao,
        firestore: FirebaseFirestore
    ): CycleRecordRepository = CycleRecordRepositoryImpl(dao, firestore)

    @Provides
    fun provideCareActionRepository(
        dao: com.example.ela.data.local.dao.CareActionDao,
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): CareActionRepository = CareActionRepositoryImpl(dao, firestore, authRepository)
}