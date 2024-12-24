package com.example.recipegpt.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.recipegpt.R
import com.example.recipegpt.activities.HomeActivity

object NotificationUtils {

    private const val CHANNEL_ID = "RecipeNotifications"
    private const val CHANNEL_NAME = "Recipe Updates"

    fun createNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel (for Android O+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW // Low priority suppresses sounds
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun createPersistentNotification(
        context: Context,
        title: String,
        message: String
    ): Notification {
        return createNotification(context, title, message, isOngoing = true)
    }

    fun updatePersistentNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val notification = createNotification(context, title, message, isOngoing = true)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification) // Update silently
    }

    fun showStandardNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val notification = createNotification(context, title, message, isOngoing = false)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotification(
        context: Context,
        title: String,
        message: String,
        isOngoing: Boolean
    ): Notification {
        // Intent to open the app's main activity
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_cooking_quote_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority prevents sound
            .setAutoCancel(!isOngoing) // If ongoing, don't auto-cancel
            .setOngoing(isOngoing) // Persistent notification if true
            .setContentIntent(pendingIntent) // Set the click action
            .build()
    }
}
