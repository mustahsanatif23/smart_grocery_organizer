package com.example.smartgroceryorganizer

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpiryNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                val context = applicationContext
                val sharedPreferences = context.getSharedPreferences("SmartGroceryOrganizerPrefs", Context.MODE_PRIVATE)
                val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
                val autoDeleteEnabled = sharedPreferences.getBoolean("auto_delete_expired", true)
                val expiryWarningDays = sharedPreferences.getInt("expiry_warning_days", 3)

                if (!notificationsEnabled) {
                    Log.d("ExpiryNotificationWorker", "Notifications disabled by user")
                    return@withContext Result.success()
                }

                val database = GroceryDatabase.getDatabase(applicationContext)
                val repository = GroceryRepository(database.groceryDao())

                if (autoDeleteEnabled) {
                    val deletedCount = repository.deleteExpiredItemsWithTracking(context)
                    if (deletedCount > 0) {
                        Log.d("ExpiryNotificationWorker", "Auto-deleted $deletedCount expired items")
                    }
                }

                /** Query all items and filter by dynamic expiry warning (avoids hardcoded query limits) */
                val allItems = database.groceryDao().getAllItemsList()
                val expiringItems = allItems.filter {
                    it.daysLeft in 0..expiryWarningDays
                }

                if (expiringItems.isNotEmpty()) {
                    Log.d("ExpiryNotificationWorker", "Found ${expiringItems.size} items expiring within $expiryWarningDays days")

                    withContext(Dispatchers.Main) {
                        NotificationHelper.sendExpiringItemsNotification(
                            applicationContext,
                            expiringItems
                        )
                    }
                } else {
                    Log.d("ExpiryNotificationWorker", "No expiring items found")
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("ExpiryNotificationWorker", "Error in worker", e)
            Result.failure()
        }
    }
}
