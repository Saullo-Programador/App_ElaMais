package com.example.ela.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ela.domain.usecase.cycle_record.SaveCycleRecordUseCase
import com.example.ela.notification.scheduler.NotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var saveCycleRecordUseCase: SaveCycleRecordUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val notificationId = intent.getIntExtra("notification_id", -1)

        when (action) {
            ACTION_PERIOD_STARTED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    saveCycleRecordUseCase(System.currentTimeMillis())
                    notificationScheduler.cancelAllMenstruationReminders()
                }
            }
            ACTION_NOT_YET -> {
                // Apenas fecha a notificação, o que o cancelNotification já fará abaixo
            }
        }

        // Fecha a notificação após a ação
        if (notificationId != -1) {
            val notificationHelper = NotificationHelper(context)
            notificationHelper.cancelNotification(notificationId)
        }
    }

    companion object {
        const val ACTION_PERIOD_STARTED = "com.example.ela.ACTION_PERIOD_STARTED"
        const val ACTION_NOT_YET = "com.example.ela.ACTION_NOT_YET"
    }
}
