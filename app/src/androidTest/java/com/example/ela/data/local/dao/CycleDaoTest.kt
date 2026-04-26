package com.example.ela.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ela.data.local.database.AppDatabase
import com.example.ela.data.local.entity.CycleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class CycleDaoTest {

    private lateinit var cycleDao: CycleDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        cycleDao = db.cycleDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetCycle() = runBlocking {
        val cycle = CycleEntity(
            id = 1,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = 123456789L
        )
        cycleDao.insertCycle(cycle)
        val loaded = cycleDao.getCycle().first()
        assertEquals(cycle.id, loaded?.id)
        assertEquals(cycle.cycleLength, loaded?.cycleLength)
    }

    @Test
    fun updateCycle() = runBlocking {
        val cycle = CycleEntity(id = 1, cycleLength = 28, periodLength = 5, lastPeriodStart = 100L)
        cycleDao.insertCycle(cycle)
        
        val updatedCycle = cycle.copy(cycleLength = 30)
        cycleDao.updateCycle(updatedCycle)
        
        val loaded = cycleDao.getCycle().first()
        assertEquals(30, loaded?.cycleLength)
    }
}
