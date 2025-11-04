package com.example.smartgroceryorganizer

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
}
