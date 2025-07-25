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

    /**
     * Cria o canal de notificação (necessário para Android 8.0+).
     * Esta função era idêntica em ambas as versões.
     */
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

    /**
     * Agenda uma notificação ÚNICA e EXATA para um horário específico.
     */
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

        Log.d("NotificationScheduler", "Agendando notificação ÚNICA ID: $notificationId para ${java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(triggerAtMillis))}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                Log.d("NotificationScheduler", "Alarme exato agendado com sucesso para ID: $notificationId (API S+).")
            } else {
                Log.w("NotificationScheduler", "Permissão para alarmes exatos não concedida para ID: $notificationId. Usando alarme não-exato como fallback.")
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            Log.d("NotificationScheduler", "Alarme exato agendado com sucesso para ID: $notificationId (pre-API S).")
        }
    }

    /**
     * UNIFICADO: Função da segunda versão para agendar notificações REPETITIVAS.
     */
    fun scheduleRepeatingNotification(
        context: Context,
        title: String,
        message: String,
        triggerAtMillis: Long, // A hora do primeiro disparo
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

        Log.d("NotificationScheduler", "Agendando notificação REPETITIVA ID: $notificationId com início em ${java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(triggerAtMillis))}")

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    /**
     * UNIFICADO: Função de cancelamento corrigida e funcional, baseada na segunda versão.
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancela o alarme principal
        cancelSingleAlarm(context, alarmManager, notificationId)

        // Tenta cancelar alarmes específicos de cada dia da semana, se existirem
        val days = listOf(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
        )
        days.forEach { day ->
            val dailyNotificationId = notificationId + day
            cancelSingleAlarm(context, alarmManager, dailyNotificationId)
        }
    }

    private fun cancelSingleAlarm(context: Context, alarmManager: AlarmManager, notificationId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d("NotificationScheduler", "Alarme com ID cancelado: $notificationId")
        }
    }
}