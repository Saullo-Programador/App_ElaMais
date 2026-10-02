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
import org.junit.Assert.assertTrue
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

    private fun action(
        id: Long = 0,
        title: String = "Teste",
        phase: CyclePhase = CyclePhase.MENSTRUAL,
        isCompleted: Boolean = false
    ) = CareActionEntity(
        id = id,
        title = title,
        description = "Descrição",
        phase = phase.name,
        isCompleted = isCompleted
    )

    @Test
    fun insertAndGetByPhase() = runBlocking {
        careActionDao.insert(action(phase = CyclePhase.MENSTRUAL))
        careActionDao.insert(action(title = "Outra fase", phase = CyclePhase.OVULATION))

        val result = careActionDao.getByPhase(CyclePhase.MENSTRUAL.name).first()

        assertEquals(1, result.size)
        assertEquals("Teste", result.first().title)
    }

    @Test
    fun insertAllDeveInserirVariasAcoesDeUmaVez() = runBlocking {
        careActionDao.insertAll(
            listOf(
                action(id = 1, title = "A"),
                action(id = 2, title = "B")
            )
        )

        val result = careActionDao.getByPhase(CyclePhase.MENSTRUAL.name).first()

        assertEquals(2, result.size)
    }

    @Test
    fun updateDeveAlterarOsCamposDaAcaoExistente() = runBlocking {
        careActionDao.insert(action(id = 1, title = "Original"))

        val linhasAfetadas = careActionDao.update(action(id = 1, title = "Atualizado", isCompleted = true))

        val result = careActionDao.getByPhase(CyclePhase.MENSTRUAL.name).first()
        assertEquals(1, linhasAfetadas)
        assertEquals("Atualizado", result.first().title)
        assertTrue(result.first().isCompleted)
    }

    @Test
    fun updateDeAcaoInexistenteNaoDeveAlterarNada() = runBlocking {
        val linhasAfetadas = careActionDao.update(action(id = 99))

        assertEquals(0, linhasAfetadas)
    }

    @Test
    fun resetAllCompletionsDeveDesmarcarTodasAsAcoesConcluidas() = runBlocking {
        careActionDao.insert(action(id = 1, isCompleted = true))
        careActionDao.insert(action(id = 2, isCompleted = true))

        careActionDao.resetAllCompletions()

        val result = careActionDao.getByPhase(CyclePhase.MENSTRUAL.name).first()
        assertTrue(result.all { !it.isCompleted })
    }

    @Test
    fun deleteByIdDeveRemoverApenasAAcaoInformada() = runBlocking {
        careActionDao.insert(action(id = 1, title = "Fica"))
        careActionDao.insert(action(id = 2, title = "Sai"))

        val linhasAfetadas = careActionDao.deleteById(2)

        val result = careActionDao.getByPhase(CyclePhase.MENSTRUAL.name).first()
        assertEquals(1, linhasAfetadas)
        assertEquals(1, result.size)
        assertEquals("Fica", result.first().title)
    }

    @Test
    fun deleteAllDeveLimparTodasAsAcoes() = runBlocking {
        careActionDao.insert(action(id = 1, phase = CyclePhase.MENSTRUAL))
        careActionDao.insert(action(id = 2, phase = CyclePhase.OVULATION))

        careActionDao.deleteAll()

        assertTrue(careActionDao.getByPhase(CyclePhase.MENSTRUAL.name).first().isEmpty())
        assertTrue(careActionDao.getByPhase(CyclePhase.OVULATION.name).first().isEmpty())
    }
}
