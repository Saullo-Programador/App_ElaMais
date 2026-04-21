package com.example.ela.domain.usecase.notification

import com.example.ela.notification.scheduler.NotificationScheduler
import javax.inject.Inject

class ScheduleCycleNotificationsUseCase @Inject constructor(
    private val notificationScheduler: NotificationScheduler
) {
    operator fun invoke(
        cycleId: Long,
        lastPeriodStart: Long,
        cycleLength: Int
    ) {
        // Agenda notificações para período fértil
        notificationScheduler.scheduleFertileWindowNotifications(
            cycleId = cycleId,
            lastPeriodStart = lastPeriodStart,
            cycleLength = cycleLength
        )

        // Agenda notificações para menstruação
        notificationScheduler.scheduleMenstruationNotifications(
            cycleId = cycleId,
            lastPeriodStart = lastPeriodStart,
            cycleLength = cycleLength
        )
    }
}