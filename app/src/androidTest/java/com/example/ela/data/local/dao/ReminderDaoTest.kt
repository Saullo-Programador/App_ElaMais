package com.example.ela.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ela.data.local.database.AppDatabase
import com.example.ela.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {

    private lateinit var reminderDao: ReminderDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        reminderDao = db.reminderDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetReminder() = runBlocking {
        val reminder = ReminderEntity(
            title = "Teste",
            description = "Desc",
            date = 12345L,
            type = "Tipo"
        )
        val id = reminderDao.insert(reminder)
        val loaded = reminderDao.getAll().first()

        assertEquals(1, loaded.size)
        assertEquals(id, loaded[0].id)
        assertEquals(reminder.title, loaded[0].title)
    }

    @Test
    fun deleteReminder() = runBlocking {
        val reminder = ReminderEntity(title = "Teste", description = "Desc", date = 12345L, type = "Tipo")
        val id = reminderDao.insert(reminder)
        val entity = ReminderEntity(id = id, title = "Teste", description = "Desc", date = 12345L, type = "Tipo")

        reminderDao.delete(entity)
        val loaded = reminderDao.getAll().first()

        assertEquals(0, loaded.size)
    }

    @Test
    fun deleteAllReminders() = runBlocking {
        reminderDao.insert(ReminderEntity(title = "T1", description = "D1", date = 1L, type = "Type"))
        reminderDao.insert(ReminderEntity(title = "T2", description = "D2", date = 2L, type = "Type"))

        reminderDao.deleteAll()
        val loaded = reminderDao.getAll().first()

        assertEquals(0, loaded.size)
    }
}
