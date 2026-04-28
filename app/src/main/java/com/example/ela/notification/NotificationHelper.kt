package com.example.ela.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ela.MainActivity
import com.example.ela.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "ela_notifications_channel"
        const val CHANNEL_NAME = "Notificações Ela+"
        const val CHANNEL_DESCRIPTION = "Lembretes de datas importantes, período fértil e menstruação"

        // IDs de notificação por tipo
        const val NOTIFICATION_TYPE_IMPORTANT_DATE = "important_date"
        const val NOTIFICATION_TYPE_FERTILE_WINDOW = "fertile_window"
        const val NOTIFICATION_TYPE_MENSTRUATION = "menstruation"
        const val NOTIFICATION_TYPE_PERIOD_CHECK = "period_check"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        notificationId: Int,
        title: String,
        message: String,
        type: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        // Adiciona botões se for verificação de menstruação
        if (type == NOTIFICATION_TYPE_PERIOD_CHECK) {
            val desceuIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_PERIOD_STARTED
                putExtra("notification_id", notificationId)
            }
            val desceuPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 100,
                desceuIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val aindaNaoIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_NOT_YET
                putExtra("notification_id", notificationId)
            }
            val aindaNaoPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 200,
                aindaNaoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(0, "Desceu 🩸", desceuPendingIntent)
            builder.addAction(0, "Ainda não", aindaNaoPendingIntent)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun cancelNotification(notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}