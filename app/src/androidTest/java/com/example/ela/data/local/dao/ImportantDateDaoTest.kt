package com.example.ela.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ela.data.local.database.AppDatabase
import com.example.ela.data.local.entity.ImportantDateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ImportantDateDaoTest {

    private lateinit var importantDateDao: ImportantDateDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        importantDateDao = db.importantDateDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val date = ImportantDateEntity(title = "Aniversário", date = 1_000L, isRecurring = true)

        importantDateDao.insert(date)
        val loaded = importantDateDao.getAll().first()

        assertEquals(1, loaded.size)
        assertEquals("Aniversário", loaded.first().title)
        assertTrue(loaded.first().isRecurring)
    }

    @Test
    fun getAllDeveOrdenarPelaDataCrescente() = runBlocking {
        importantDateDao.insert(ImportantDateEntity(title = "Mais tarde", date = 2_000L, isRecurring = false))
        importantDateDao.insert(ImportantDateEntity(title = "Mais cedo", date = 1_000L, isRecurring = false))

        val loaded = importantDateDao.getAll().first()

        assertEquals("Mais cedo", loaded.first().title)
        assertEquals("Mais tarde", loaded[1].title)
    }

    @Test
    fun deleteDateDeveRemoverApenasADataInformada() = runBlocking {
        val fica = ImportantDateEntity(title = "Fica", date = 1_000L, isRecurring = false)
        importantDateDao.insert(fica)
        importantDateDao.insert(ImportantDateEntity(title = "Sai", date = 2_000L, isRecurring = false))

        val loaded = importantDateDao.getAll().first()
        val saiEntity = loaded.first { it.title == "Sai" }

        importantDateDao.delete(saiEntity)

        val result = importantDateDao.getAll().first()
        assertEquals(1, result.size)
        assertEquals("Fica", result.first().title)
    }

    @Test
    fun deleteAllDeveLimparTodasAsDatas() = runBlocking {
        importantDateDao.insert(ImportantDateEntity(title = "A", date = 1_000L, isRecurring = false))
        importantDateDao.insert(ImportantDateEntity(title = "B", date = 2_000L, isRecurring = false))

        importantDateDao.deleteAll()

        assertTrue(importantDateDao.getAll().first().isEmpty())
    }
}
