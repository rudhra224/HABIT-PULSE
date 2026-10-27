package com.example.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitTitle = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Your Daily Habits"
        val habitMessage = intent.getStringExtra(EXTRA_HABIT_MESSAGE) ?: "Time to keep your streak alive! Consistent actions create extraordinary results."
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)

        NotificationHelper.createNotificationChannel(context)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(habitTitle)
            .setContentText(habitMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(habitMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val EXTRA_HABIT_TITLE = "extra_habit_title"
        const val EXTRA_HABIT_MESSAGE = "extra_habit_message"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
