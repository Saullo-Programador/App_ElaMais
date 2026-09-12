package com.example.ela.domain.usecase.notification

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ela.domain.repository.PreferencesRepository
import com.example.ela.notification.scheduler.NotificationScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ScheduleCycleNotificationsUseCase @Inject constructor(
    private val notificationScheduler: NotificationScheduler,
    private val preferencesRepository: PreferencesRepository
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(
        cycleId: Long,
        lastPeriodStart: Long,
        cycleLength: Int
    ) {
        val preferences = preferencesRepository.getPreferences().first() ?: return

        // Agenda notificações para período fértil
        notificationScheduler.scheduleFertileWindowNotifications(
            cycleId = cycleId,
            lastPeriodStart = lastPeriodStart,
            cycleLength = cycleLength,
            preferences = preferences,
        )

        // Agenda notificações para menstruação
        notificationScheduler.scheduleMenstruationNotifications(
            cycleId = cycleId,
            lastPeriodStart = lastPeriodStart,
            cycleLength = cycleLength,
            preferences = preferences
        )
    }
}
