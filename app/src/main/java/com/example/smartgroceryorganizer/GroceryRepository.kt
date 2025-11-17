package com.example.smartgroceryorganizer

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData

class GroceryRepository(private val groceryDao: GroceryDao) {

    val allItems: LiveData<List<GroceryItem>> = groceryDao.getAllItems()

    suspend fun insert(item: GroceryItem) {
        groceryDao.insertItem(item)
    }

    suspend fun update(item: GroceryItem) {
        groceryDao.updateItem(item)
    }

    suspend fun delete(item: GroceryItem) {
        groceryDao.deleteItem(item)
    }

    suspend fun deleteById(itemId: Int) {
        groceryDao.deleteItemById(itemId)
    }

    suspend fun getItemById(itemId: Int): GroceryItem? {
        return try {
            groceryDao.getItemById(itemId)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getExpiringSoonItems(): List<GroceryItem> {
        return groceryDao.getExpiringSoonItems()
    }

    suspend fun deleteExpiredItems(): Int {
        return groceryDao.deleteExpiredItems()
    }

    suspend fun getExpiredItemsCount(): Int {
        return groceryDao.getExpiredCount()
    }

    /** Delete expired items and track the count for analytics */
    suspend fun deleteExpiredItemsWithTracking(context: Context): Int {
        val expiredCount = groceryDao.getExpiredCount()
        if (expiredCount > 0) {
            val deletedCount = groceryDao.deleteExpiredItems()

            // Update cumulative expired count in SharedPreferences for analytics
            val sharedPreferences = context.getSharedPreferences("SmartGroceryOrganizerPrefs", Context.MODE_PRIVATE)
            val currentTotal = sharedPreferences.getInt("total_expired_items", 0)
            sharedPreferences.edit {
                putInt("total_expired_items", currentTotal + deletedCount)
            }

            return deletedCount
        }
        return 0
    }
}
