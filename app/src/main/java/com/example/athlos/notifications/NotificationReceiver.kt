package com.example.athlos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {

    private val CHANNEL_ID = "athlos_notification_channel"
    private val NOTIFICATION_ID = 101

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val title = intent?.getStringExtra("notification_title") ?: "Sua Notificação Athlos"
            val message = intent?.getStringExtra("notification_message") ?: "Este é um lembrete do aplicativo Athlos!"

            createNotificationChannel(it)

            val builder = NotificationCompat.Builder(it, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(it)) {
                notify(NOTIFICATION_ID, builder.build())
            }

            println("Notification received and displayed at: ${System.currentTimeMillis()}")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Athlos Reminders"
            val descriptionText = "Channel for Athlos app notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}