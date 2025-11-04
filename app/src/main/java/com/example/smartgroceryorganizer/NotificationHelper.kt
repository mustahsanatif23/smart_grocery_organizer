package com.example.smartgroceryorganizer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "expiring_items_channel"
    private const val CHANNEL_NAME = "Expiring Items"
    private const val CHANNEL_DESCRIPTION = "Notifications for grocery items expiring soon"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @Suppress("MissingPermission")
    fun sendExpiringItemsNotification(context: Context, expiringItems: List<GroceryItem>) {
        if (expiringItems.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = when (expiringItems.size) {
            1 -> "1 item expiring soon!"
            else -> "${expiringItems.size} items expiring soon!"
        }

        val message = buildNotificationMessage(expiringItems)

        val iconResId = try {
            R.drawable.ic_notification
        } catch (e: Exception) {
            android.R.drawable.ic_dialog_alert
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconResId)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun buildNotificationMessage(items: List<GroceryItem>): String {
        return when {
            items.size == 1 -> {
                val item = items[0]
                when (item.daysLeft) {
                    0 -> "${item.name} expires today!"
                    1 -> "${item.name} expires tomorrow!"
                    else -> "${item.name} expires in ${item.daysLeft} days!"
                }
            }
            items.size <= 3 -> {
                items.joinToString(", ") {
                    when (it.daysLeft) {
                        0 -> "${it.name} (today)"
                        1 -> "${it.name} (tomorrow)"
                        else -> "${it.name} (${it.daysLeft} days)"
                    }
                }
            }
            else -> {
                val first3 = items.take(3).joinToString(", ") { it.name }
                "$first3 and ${items.size - 3} more items are expiring soon!"
            }
        }
    }
}

