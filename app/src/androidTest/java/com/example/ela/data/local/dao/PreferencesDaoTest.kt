package com.example.ela.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ela.data.local.database.AppDatabase
import com.example.ela.data.local.entity.PreferencesEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PreferencesDaoTest {

    private lateinit var preferencesDao: PreferencesDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        preferencesDao = db.preferencesDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun preferences(isDarkMode: Boolean = false) = PreferencesEntity(
        favoriteFoods = "[]",
        favoriteSweets = "[]",
        dislikedThings = "[]",
        symptoms = "[]",
        isDarkMode = isDarkMode
    )

    @Test
    fun getSemNenhumRegistroSalvoDeveRetornarNull() = runBlocking {
        assertNull(preferencesDao.get().first())
    }

    @Test
    fun saveAndGet() = runBlocking {
        preferencesDao.save(preferences())

        val loaded = preferencesDao.get().first()

        assertEquals("[]", loaded?.favoriteFoods)
    }

    @Test
    fun saveDeveSubstituirORegistroExistente() = runBlocking {
        preferencesDao.save(preferences().copy(id = 1, notificationsTime = "08:00"))
        preferencesDao.save(preferences().copy(id = 1, notificationsTime = "20:00"))

        val loaded = preferencesDao.get().first()

        assertEquals("20:00", loaded?.notificationsTime)
    }

    @Test
    fun updateDarkModeDeveAlterarApenasEsseCampo() = runBlocking {
        preferencesDao.save(preferences(isDarkMode = false).copy(id = 1, notificationsTime = "09:00"))

        preferencesDao.updateDarkMode(true)

        val loaded = preferencesDao.get().first()
        assertTrue(loaded?.isDarkMode == true)
        assertEquals("09:00", loaded?.notificationsTime)
    }

    @Test
    fun deleteAllDeveLimparAsPreferencias() = runBlocking {
        preferencesDao.save(preferences())

        preferencesDao.deleteAll()

        assertNull(preferencesDao.get().first())
    }
}
