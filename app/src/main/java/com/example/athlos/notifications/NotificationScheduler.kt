package com.example.athlos

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.athlos.notifications.NotificationReceiver
import java.util.Calendar

object NotificationScheduler {

    const val NOTIFICATION_CHANNEL_ID = "athlos_notifications_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Athlos Reminders"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Canal para lembretes e notificações do Athlos"

    const val CUSTOM_NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NOTIFICATION_CHANNEL_NAME
            val descriptionText = NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationScheduler", "Notification channel created: $NOTIFICATION_CHANNEL_ID")
        }
    }

    fun scheduleNotification(
        context: Context,
        title: String,
        message: String,
        triggerAtMillis: Long,
        notificationId: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
            putExtra("notification_title", title)
            putExtra("notification_message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("NotificationScheduler", "Attempting to schedule notification ID: $notificationId " +
                "at ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(triggerAtMillis))} " +
                "Title: '$title', Message: '$message'")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
                Log.d("NotificationScheduler", "Exact alarm scheduled successfully for ID: $notificationId (API S+).")
            } else {
                Log.w("NotificationScheduler", "Cannot schedule exact alarms for ID: $notificationId. User needs to grant permission (ACTION_REQUEST_SCHEDULE_EXACT_ALARM). Falling back to inexact alarm if available, otherwise it won't be scheduled.")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d("NotificationScheduler", "Exact alarm scheduled successfully for ID: $notificationId (pre-API S).")
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d("NotificationScheduler", "Cancelled notification ID: $notificationId")
        } ?: run {
            Log.d("NotificationScheduler", "No pending intent found for ID: $notificationId to cancel.")
        }
    }
}