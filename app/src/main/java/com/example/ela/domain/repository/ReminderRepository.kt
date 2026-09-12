package com.example.ela.domain.repository

import com.example.ela.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getReminder(): Flow<List<Reminder>>

    suspend fun saveReminder(reminder: Reminder)

    suspend fun deleteReminder(reminder: Reminder)

    suspend fun syncReminder()
}