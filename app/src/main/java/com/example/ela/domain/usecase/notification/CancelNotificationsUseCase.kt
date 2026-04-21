package com.example.ela.domain.usecase.notification

import com.example.ela.notification.scheduler.NotificationScheduler
import javax.inject.Inject

class CancelNotificationsUseCase @Inject constructor(
    private val notificationScheduler: NotificationScheduler
) {
    fun cancelImportantDateNotifications(dateId: Long) {
        notificationScheduler.cancelImportantDateNotifications(dateId)
    }

    fun cancelCycleNotifications(cycleId: Long) {
        notificationScheduler.cancelFertileWindowNotifications(cycleId)
        notificationScheduler.cancelMenstruationNotifications(cycleId)
    }

    fun cancelAllNotifications() {
        notificationScheduler.cancelAllNotifications()
    }
}