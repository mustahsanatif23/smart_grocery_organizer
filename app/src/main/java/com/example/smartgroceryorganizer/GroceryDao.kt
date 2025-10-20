package com.example.smartgroceryorganizer

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GroceryDao {

    @Query("SELECT * FROM grocery_items ORDER BY daysLeft ASC")
    fun getAllItems(): LiveData<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE id = :itemId")
    suspend fun getItemById(itemId: Int): GroceryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GroceryItem)

    @Update
    suspend fun updateItem(item: GroceryItem)

    @Delete
    suspend fun deleteItem(item: GroceryItem)

    @Query("DELETE FROM grocery_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Int)

    @Query("SELECT * FROM grocery_items WHERE category = :category ORDER BY daysLeft ASC")
    fun getItemsByCategory(category: String): LiveData<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchItems(query: String): LiveData<List<GroceryItem>>

    @Query("DELETE FROM grocery_items")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM grocery_items WHERE daysLeft <= 3 AND daysLeft >= 0 ORDER BY daysLeft ASC")
    suspend fun getExpiringSoonItems(): List<GroceryItem>

    @Query("SELECT * FROM grocery_items WHERE daysLeft < 0")
    suspend fun getExpiredItems(): List<GroceryItem>

    @Query("DELETE FROM grocery_items WHERE daysLeft < 0")
    suspend fun deleteExpiredItems(): Int
}
