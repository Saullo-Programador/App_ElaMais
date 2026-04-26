package com.example.ela.domain.usecase.notification

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ela.domain.repository.PreferencesRepository
import com.example.ela.notification.scheduler.NotificationScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ScheduleImportantDateNotificationUseCase @Inject constructor(
    private val notificationScheduler: NotificationScheduler,
    private val preferencesRepository: PreferencesRepository,
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(
        dateId: Long,
        title: String,
        dateMillis: Long
    ) {
        val preferences = preferencesRepository.getPreferences().first() ?: return
        notificationScheduler.scheduleImportantDateNotification(
            dateId = dateId,
            title = title,
            dateMillis = dateMillis,
            preferences = preferences
        )
    }
}