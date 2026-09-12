package com.example.ela.notification.scheduler

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.ela.domain.model.Preferences
import com.example.ela.notification.worker.NotificationWorker
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    companion object {
        // Tipos de notificação
        const val TYPE_IMPORTANT_DATE = "important_date"
        const val TYPE_FERTILE_WINDOW = "fertile_window"
        const val TYPE_MENSTRUATION = "menstruation"
        const val TYPE_PERIOD_CHECK = "period_check"

        // Antecedência das notificações
        const val DAYS_BEFORE_3 = 3L
        const val DAYS_BEFORE_1 = 1L
        const val DAYS_BEFORE_0 = 0L
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * Agenda notificações para uma data importante
     * Notifica 3 dias antes, 1 dia antes e no dia
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleImportantDateNotification(
        dateId: Long,
        title: String,
        dateMillis: Long,
        preferences: Preferences
    ) {
        val baseDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // Cancela notificações existentes
        cancelImportantDateNotifications(dateId)

        // Agenda notificações para 3 dias antes, 1 dia antes e no dia
        scheduleNotification(
            notificationId = generateNotificationId(dateId, TYPE_IMPORTANT_DATE, DAYS_BEFORE_3),
            title = title,
            message = "$title em 3 dias",
            type = TYPE_IMPORTANT_DATE,
            targetDate = baseDate.minusDays(DAYS_BEFORE_3),
            preferences = preferences
        )

        scheduleNotification(
            notificationId = generateNotificationId(dateId, TYPE_IMPORTANT_DATE, DAYS_BEFORE_1),
            title = title,
            message = "$title amanhã",
            type = TYPE_IMPORTANT_DATE,
            targetDate = baseDate.minusDays(DAYS_BEFORE_1),
            preferences = preferences
        )

        scheduleNotification(
            notificationId = generateNotificationId(dateId, TYPE_IMPORTANT_DATE, DAYS_BEFORE_0),
            title = title,
            message = "$title é hoje!",
            type = TYPE_IMPORTANT_DATE,
            targetDate = baseDate,
            preferences = preferences
        )
    }

    /**
     * Agenda notificações para período fértil
     * Calcula o período fértil baseado no ciclo e agenda notificações
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleFertileWindowNotifications(
        cycleId: Long,
        lastPeriodStart: Long,
        cycleLength: Int,
        preferences: Preferences
    ) {
        val periodStart = Instant.ofEpochMilli(lastPeriodStart)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // Calcula período fértil (geralmente 14 dias antes da próxima menstruação)
        val fertileWindowStart = periodStart.plusDays((cycleLength - 16).toLong())
        val fertileWindowEnd = periodStart.plusDays((cycleLength - 12).toLong())

        // Cancela notificações existentes
        cancelFertileWindowNotifications(cycleId)

        // Notifica 3 dias antes do início do período fértil
        scheduleNotification(
            notificationId = generateNotificationId(cycleId, TYPE_FERTILE_WINDOW, DAYS_BEFORE_3),
            title = "Período Fértil",
            message = "Seu período fértil começa em 3 dias",
            type = TYPE_FERTILE_WINDOW,
            targetDate = fertileWindowStart.minusDays(DAYS_BEFORE_3),
            preferences = preferences
        )

        // Notifica 1 dia antes
        scheduleNotification(
            notificationId = generateNotificationId(cycleId, TYPE_FERTILE_WINDOW, DAYS_BEFORE_1),
            title = "Período Fértil",
            message = "Seu período fértil começa amanhã",
            type = TYPE_FERTILE_WINDOW,
            targetDate = fertileWindowStart.minusDays(DAYS_BEFORE_1),
            preferences = preferences
        )

        // Notifica no primeiro dia do período fértil
        scheduleNotification(
            notificationId = generateNotificationId(cycleId, TYPE_FERTILE_WINDOW, DAYS_BEFORE_0),
            title = "Período Fértil",
            message = "Você está no período fértil!",
            type = TYPE_FERTILE_WINDOW,
            targetDate = fertileWindowStart,
            preferences = preferences
        )
    }

    /**
     * Agenda notificações para menstruação
     * Notifica 3 dias antes, 1 dia antes e no primeiro dia
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleMenstruationNotifications(
        cycleId: Long,
        lastPeriodStart: Long,
        cycleLength: Int,
        preferences: Preferences
    ) {
        val periodStart = Instant.ofEpochMilli(lastPeriodStart)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // Próxima menstruação = última menstruação + duração do ciclo
        val nextPeriodStart = periodStart.plusDays(cycleLength.toLong())

        // Cancela notificações existentes
        cancelMenstruationNotifications(cycleId)

        // Notifica 3 dias antes
        scheduleNotification(
            notificationId = generateNotificationId(cycleId, TYPE_MENSTRUATION, DAYS_BEFORE_3),
            title = "Menstruação",
            message = "Sua menstruação deve começar em 3 dias",
            type = TYPE_MENSTRUATION,
            targetDate = nextPeriodStart.minusDays(DAYS_BEFORE_3),
            preferences = preferences
        )

        // Notifica 1 dia antes
        scheduleNotification(
            notificationId = generateNotificationId(cycleId, TYPE_MENSTRUATION, DAYS_BEFORE_1),
            title = "Menstruação",
            message = "Sua menstruação deve começar amanhã",
            type = TYPE_MENSTRUATION,
            targetDate = nextPeriodStart.minusDays(DAYS_BEFORE_1),
            preferences = preferences
        )

        // Notifica no dia
        scheduleNotification(
            notificationId = generateNotificationId(cycleId, TYPE_MENSTRUATION, DAYS_BEFORE_0),
            title = "Menstruação",
            message = "Sua menstruação deve começar hoje",
            type = TYPE_MENSTRUATION,
            targetDate = nextPeriodStart,
            preferences = preferences
        )

        // Agenda verificações diárias (3x ao dia) perto da data
        schedulePeriodCheckNotifications(cycleId, nextPeriodStart, preferences)
    }

    /**
     * Agenda verificações de "Já desceu?" respeitando as preferências da usuária
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun schedulePeriodCheckNotifications(
        cycleId: Long,
        nextPeriodStart: LocalDate,
        preferences: Preferences
    ) {
        val daysToCheck = listOf(-2L, -1L, 0L, 1L) // 2 dias antes até 1 dia depois
        val (baseHour, baseMinute) = preferences.notificationsTime.split(":").map { it.toInt() }
        
        // Usamos o número de vezes configurado, garantindo pelo menos 3 para este alerta importante
        val timesPerDay = preferences.timesPerDay.coerceAtLeast(3)
        val intervalHours = 15 / timesPerDay // Distribui as notificações em um intervalo de 15 horas

        daysToCheck.forEach { dayOffset ->
            val targetDate = nextPeriodStart.plusDays(dayOffset)

            for (i in 0 until timesPerDay) {
                val now = LocalDateTime.now()
                val scheduledTime = targetDate.atTime(baseHour, baseMinute).plusHours((i * intervalHours).toLong())

                if (scheduledTime.isAfter(now)) {
                    val delay = Duration.between(now, scheduledTime).toMillis()
                    val notificationId = generateNotificationId(cycleId, TYPE_PERIOD_CHECK, dayOffset) + (i * 10)

                    val inputData = Data.Builder()
                        .putInt(NotificationWorker.KEY_NOTIFICATION_ID, notificationId)
                        .putString(NotificationWorker.KEY_TITLE, "Ela+")
                        .putString(NotificationWorker.KEY_MESSAGE, "Já desceu por aí? 🩸")
                        .putString(NotificationWorker.KEY_TYPE, TYPE_PERIOD_CHECK)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .addTag("notification_$TYPE_PERIOD_CHECK")
                        .build()

                    workManager.enqueueUniqueWork(
                        "${NotificationWorker.WORK_NAME}_period_check_${notificationId}",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            }
        }
    }

    /**
     * Agenda uma única notificação
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNotification(
        notificationId: Int,
        title: String,
        message: String,
        type: String,
        targetDate: LocalDate,
        preferences: Preferences // Passar as preferências aqui
    ) {
        val now = LocalDateTime.now()
        val (hour, minute) = preferences.notificationsTime.split(":").map { it.toInt() }

        // Intervalo entre notificações se for mais de uma vez ao dia (ex: a cada 4 horas)
        val intervalHours = if (preferences.timesPerDay > 1) 12 / preferences.timesPerDay else 0

        for (i in 0 until preferences.timesPerDay) {
            val scheduledTime = targetDate.atTime(hour, minute).plusHours((i * intervalHours).toLong())

            if (scheduledTime.isBefore(now)) continue

            val delay = Duration.between(now, scheduledTime).toMillis()

            val inputData = Data.Builder()
                .putInt(NotificationWorker.KEY_NOTIFICATION_ID, notificationId + i) // ID único para cada repetição
                .putString(NotificationWorker.KEY_TITLE, title)
                .putString(NotificationWorker.KEY_MESSAGE, message)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("notification_$type")
                .build()

            workManager.enqueueUniqueWork(
                "${NotificationWorker.WORK_NAME}_${notificationId}_$i",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    /**
     * Cancela todas as notificações relacionadas à menstruação (lembretes e verificações)
     */
    fun cancelAllMenstruationReminders() {
        workManager.cancelAllWorkByTag("notification_$TYPE_MENSTRUATION")
        workManager.cancelAllWorkByTag("notification_$TYPE_PERIOD_CHECK")
    }

    /**
     * Cancela notificações de uma data importante
     */
    fun cancelImportantDateNotifications(dateId: Long) {
        cancelNotificationsForType(dateId, TYPE_IMPORTANT_DATE)
    }

    /**
     * Cancela notificações do período fértil
     */
    fun cancelFertileWindowNotifications(cycleId: Long) {
        cancelNotificationsForType(cycleId, TYPE_FERTILE_WINDOW)
    }

    /**
     * Cancela notificações de menstruação
     */
    fun cancelMenstruationNotifications(cycleId: Long) {
        cancelNotificationsForType(cycleId, TYPE_MENSTRUATION)
        workManager.cancelAllWorkByTag("notification_$TYPE_PERIOD_CHECK")
    }

    private fun cancelNotificationsForType(id: Long, type: String) {
        listOf(DAYS_BEFORE_3, DAYS_BEFORE_1, DAYS_BEFORE_0).forEach { daysBefore ->
            val notificationId = generateNotificationId(id, type, daysBefore)
            val workName = "${NotificationWorker.WORK_NAME}_$notificationId"
            workManager.cancelUniqueWork(workName)
        }
    }

    /**
     * Gera ID único para notificação baseado no ID original, tipo e dias de antecedência
     */
    private fun generateNotificationId(id: Long, type: String, daysBefore: Long): Int {
        return when (type) {
            TYPE_IMPORTANT_DATE -> (id * 100 + daysBefore).toInt()
            TYPE_FERTILE_WINDOW -> (id * 1000 + 100 + daysBefore).toInt()
            TYPE_MENSTRUATION -> (id * 1000 + 200 + daysBefore).toInt()
            TYPE_PERIOD_CHECK -> (id * 1000 + 300 + (daysBefore + 10)).toInt()
            else -> id.toInt()
        }
    }

    /**
     * Cancela todas as notificações
     */
    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag(NotificationWorker.WORK_NAME)
    }
}