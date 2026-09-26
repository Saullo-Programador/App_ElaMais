package com.example.ela.data.repository

import android.util.Log
import com.example.ela.data.local.dao.PreferencesDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.PreferencesDto
import com.example.ela.domain.model.Preferences
import com.example.ela.domain.repository.AuthRepository
import com.example.ela.domain.repository.PreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PreferencesRepositoryImpl(
    private val dao: PreferencesDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : PreferencesRepository {

    private fun getPreferencesDocument() =
        authRepository.getCurrentUser()?.uid?.let { uid ->
            firestore.collection("preferences").document(uid)
        }

    override fun getPreferences(): Flow<Preferences?> {
        return dao.get().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun savePreferences(preferences: Preferences) {
        dao.save(preferences.toEntity())

        try {
            getPreferencesDocument()
                ?.set(preferences.toDto())
                ?.await()
        } catch (e: Exception) {
            Log.e(
                "PreferencesRepository",
                "Erro ao sincronizar Preferências do Firebase",
                e
            )
        }
    }

    override suspend fun syncPreferences() {
        try {
            val snapshot = getPreferencesDocument()?.get()?.await()
            val dto = snapshot?.toObject(PreferencesDto::class.java)

            dto?.let {
                dao.save(it.toDomain().toEntity())
            }
        } catch (e: Exception) {
            Log.e(
                "PreferencesRepository",
                "Erro ao sincronizar Preferências do Firebase",
                e
            )
        }
    }

    override suspend fun updateDarkMode(isDarkMode: Boolean) {
        val current = dao.get().first()?.toDomain() ?: Preferences()
        val updated = current.copy(isDarkMode = isDarkMode)
        savePreferences(updated)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
        try {
            getPreferencesDocument()?.delete()?.await()
        } catch (e: Exception) {
            Log.e("PreferencesRepository", "Erro ao deletar preferências no Firebase", e)
        }
    }

}