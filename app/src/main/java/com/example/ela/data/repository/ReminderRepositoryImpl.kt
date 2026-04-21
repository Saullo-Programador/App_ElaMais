package com.example.ela.data.repository

import com.example.ela.data.local.dao.ReminderDao
import com.example.ela.data.mapper.toDomain
import com.example.ela.data.mapper.toDto
import com.example.ela.data.mapper.toEntity
import com.example.ela.data.remote.dto.ReminderDto
import com.example.ela.domain.model.Reminder
import com.example.ela.domain.repository.ReminderRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ReminderRepositoryImpl (
    private val dao: ReminderDao,
    private val firestore: FirebaseFirestore
) : ReminderRepository {

    private val collection = firestore.collection("reminders")

    override fun getReminder(): Flow<List<Reminder>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveReminder(reminder: Reminder) {
        dao.insert(reminder.toEntity())
        try {
            collection.document(reminder.id.toString())
                .set(reminder.toDto())
                .await()
        } catch (e: Exception) {
            // falhou? tudo bem, já salvou local
        }
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        dao.delete(reminder.toEntity())
        try {
            collection.document(reminder.id.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            // falhou? tudo bem, já salvou local
        }
    }

    override suspend fun syncReminder() {
        try {
            val snapshot = collection.get().await()

            val list = snapshot.documents.mapNotNull {
                it.toObject(ReminderDto::class.java)
            }

            list.forEach {
                dao.insert(it.toDomain().toEntity())
            }
        }catch (e: Exception){
            // sem internet -> ignora
        }
    }

}