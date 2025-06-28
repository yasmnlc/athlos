package com.example.athlos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object NotificationScheduler {
    fun scheduleNotification(
        context: Context,
        title: String,
        message: String,
        triggerAtMillis: Long
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_title", title)
            putExtra("notification_message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun scheduleNotificationForMinutes(context: Context, minutes: Int, title: String, message: String) {
        val triggerTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, minutes)
        }.timeInMillis

        scheduleNotification(context, title, message, triggerTime)
    }
}