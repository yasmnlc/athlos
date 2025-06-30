package com.example.athlos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.util.Log
import com.example.athlos.R

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "athlos_notification_channel"
        const val DEFAULT_NOTIFICATION_TITLE = "Seu Lembrete Athlos"
        const val DEFAULT_NOTIFICATION_MESSAGE = "Este é um lembrete do aplicativo Athlos!"
        const val ACTION_DISMISS_NOTIFICATION = "com.example.athlos.DISMISS_NOTIFICATION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val notificationId = intent?.getIntExtra("notification_id", 0) ?: 0
            val title = intent?.getStringExtra("notification_title") ?: DEFAULT_NOTIFICATION_TITLE
            val message = intent?.getStringExtra("notification_message") ?: DEFAULT_NOTIFICATION_MESSAGE
            val action = intent?.action

            Log.d("NotificationReceiver", "Received broadcast. ID: $notificationId, Title: '$title', Message: '$message', Action: $action")

            val notificationManager = NotificationManagerCompat.from(it)

            if (action == ACTION_DISMISS_NOTIFICATION) {
                notificationManager.cancel(notificationId)
                Log.d("NotificationReceiver", "Dismissed notification ID: $notificationId via action.")
                return
            }

            createNotificationChannel(it)

            val dismissIntent = Intent(it, NotificationReceiver::class.java).apply {
                setAction(ACTION_DISMISS_NOTIFICATION)
                putExtra("notification_id", notificationId)
            }

            val dismissPendingIntent = PendingIntent.getBroadcast(
                it,
                notificationId,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(it, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .addAction(0, "OK", dismissPendingIntent)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(it, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    notificationManager.notify(notificationId, builder.build())
                    Log.d("NotificationReceiver", "Notification shown for ID: $notificationId")
                } catch (e: SecurityException) {
                    Log.e("NotificationReceiver", "SecurityException when showing notification: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                Log.w("NotificationReceiver", "POST_NOTIFICATIONS permission not granted. Cannot show notification for ID: $notificationId.")
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = com.example.athlos.NotificationScheduler.NOTIFICATION_CHANNEL_NAME
            val descriptionText = com.example.athlos.NotificationScheduler.NOTIFICATION_CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationReceiver", "Notification channel created (from receiver): $CHANNEL_ID")
        }
    }
}