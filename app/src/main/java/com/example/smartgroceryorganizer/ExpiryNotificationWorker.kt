package com.example.smartgroceryorganizer

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpiryNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        return withContext(Dispatchers.IO) {
            try {
                val database = GroceryDatabase.getDatabase(applicationContext)
                val repository = GroceryRepository(database.groceryDao())

                // First, delete expired items (daysLeft < 0)
                val deletedCount = repository.deleteExpiredItems()
                if (deletedCount > 0) {
                    Log.d("ExpiryNotificationWorker", "Auto-deleted $deletedCount expired items")
                }

                // Then, get items expiring soon (0 <= daysLeft <= 3)
                val expiringItems = repository.getExpiringSoonItems()

                if (expiringItems.isNotEmpty()) {
                    Log.d("ExpiryNotificationWorker", "Found ${expiringItems.size} expiring items")

                    // Send notification
                    withContext(Dispatchers.Main) {
                        NotificationHelper.sendExpiringItemsNotification(
                            applicationContext,
                            expiringItems
                        )
                    }
                } else {
                    Log.d("ExpiryNotificationWorker", "No expiring items found")
                }

                ListenableWorker.Result.success()
            } catch (e: Exception) {
                Log.e("ExpiryNotificationWorker", "Error in worker", e)
                ListenableWorker.Result.failure()
            }
        }
    }
}

