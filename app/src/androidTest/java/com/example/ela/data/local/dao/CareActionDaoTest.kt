package com.example.ela.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ela.data.local.database.AppDatabase
import com.example.ela.data.local.entity.CareActionEntity
import com.example.ela.domain.model.CyclePhase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class CareActionDaoTest {

    private lateinit var careActionDao: CareActionDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        careActionDao = db.careActionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetByPhase() = runBlocking {
        val phase = CyclePhase.MENSTRUAL.name
        val action = CareActionEntity(
            title = "Teste",
            description = "Desc",
            phase = phase,
            isCompleted = false
        )
        careActionDao.insert(action)
        val loaded = careActionDao.getByPhase(phase).first()

        assertEquals(1, loaded.size)
        assertEquals("Teste", loaded[0].title)
    }

    @Test
    fun updateCareAction() = runBlocking {
        val action = CareActionEntity(
            id = 1,
            title = "Teste",
            description = "Desc",
            phase = CyclePhase.MENSTRUAL.name,
            isCompleted = false
        )
        careActionDao.insert(action)

        val updated = action.copy(isCompleted = true)
        careActionDao.update(updated)

        val loaded = careActionDao.getByPhase(CyclePhase.MENSTRUAL.name).first()
        assertEquals(true, loaded[0].isCompleted)
    }

    @Test
    fun deleteCareAction() = runBlocking {
        val action = CareActionEntity(
            title = "Teste",
            description = "Desc",
            phase = CyclePhase.MENSTRUAL.name,
            isCompleted = false
        )
        val id = careActionDao.insert(action)
        val entityToDelete = action.copy(id = id)

        careActionDao.delete(entityToDelete)
        val loaded = careActionDao.getByPhase(CyclePhase.MENSTRUAL.name).first()

        assertEquals(0, loaded.size)
    }
}
