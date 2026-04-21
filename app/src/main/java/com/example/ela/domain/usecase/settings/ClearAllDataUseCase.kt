package com.example.ela.domain.usecase.settings

import com.example.ela.data.local.dao.CareActionDao
import com.example.ela.data.local.dao.CycleDao
import com.example.ela.data.local.dao.CycleRecordDao
import com.example.ela.data.local.dao.ImportantDateDao
import com.example.ela.data.local.dao.PreferencesDao
import com.example.ela.data.local.dao.ReminderDao
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val cycleDao: CycleDao,
    private val careActionDao: CareActionDao,
    private val importantDateDao: ImportantDateDao,
    private val reminderDao: ReminderDao,
    private val cycleRecordDao: CycleRecordDao,
    private val preferencesDao: PreferencesDao
) {

    suspend operator fun invoke() {
        cycleDao.deleteAll()
        careActionDao.deleteAll()
        importantDateDao.deleteAll()
        reminderDao.deleteAll()
        cycleRecordDao.deleteAll()
        preferencesDao.deleteAll()
    }
}
