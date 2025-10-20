package com.example.smartgroceryorganizer

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "expiry_notification_work"

    fun scheduleExpiryNotifications(context: Context) {
        // Calculate initial delay to schedule notification at 12 PM (midday)
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If it's already past 12 PM today, schedule for 12 PM tomorrow
        if (currentTime.after(targetTime)) {
            targetTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        Log.d("NotificationScheduler", "Scheduling notification in ${initialDelay / 1000 / 60} minutes")

        // Create constraints - only run when device is not in battery saver mode
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false) // Allow even on low battery
            .build()

        // Create periodic work request - runs daily at 12 PM
        val workRequest = PeriodicWorkRequestBuilder<ExpiryNotificationWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval for optimization
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        // Enqueue the work with unique name to avoid duplicates
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

        Log.d("NotificationScheduler", "Daily expiry notifications scheduled at 12 PM")
    }

    fun cancelExpiryNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        Log.d("NotificationScheduler", "Expiry notifications cancelled")
    }
}

