package com.example.ela.data.repository

import com.example.ela.data.local.dao.PreferencesDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.PreferencesDto
import com.example.ela.domain.model.Preferences
import com.example.ela.domain.repository.PreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PreferencesRepositoryImpl(
    private val dao: PreferencesDao,
    private val firestore: FirebaseFirestore
) : PreferencesRepository {

    private val collection = firestore.collection("preferences")

    override fun getPreferences(): Flow<Preferences?> {
        return dao.get().map { it?.toDomain() }
    }

    override suspend fun savePreferences(preferences: Preferences) {
        dao.save(preferences.toEntity())

        try {
            collection.document("user_preferences")
                .set(preferences.toDto())
                .await()
        } catch (_: Exception) {}
    }

    override suspend fun syncPreferences() {
        try {
            val snapshot = collection.document("user_preferences").get().await()
            val dto = snapshot.toObject(PreferencesDto::class.java)

            dto?.let {
                dao.save(it.toDomain().toEntity())
            }
        } catch (_: Exception) {}
    }
}