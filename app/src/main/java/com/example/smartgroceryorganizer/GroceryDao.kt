package com.example.smartgroceryorganizer

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GroceryDao {

    @Query("SELECT * FROM grocery_items WHERE daysLeft >= 0 ORDER BY daysLeft ASC")
    fun getAllItems(): LiveData<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items ORDER BY daysLeft ASC")
    suspend fun getAllItemsList(): List<GroceryItem>

    @Query("SELECT * FROM grocery_items WHERE daysLeft >= 0 ORDER BY daysLeft ASC")
    suspend fun getNonExpiredItemsList(): List<GroceryItem>

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

    @Query("DELETE FROM grocery_items")
    suspend fun deleteAllItems()

    @Query("SELECT * FROM grocery_items WHERE daysLeft <= 3 AND daysLeft >= 0 ORDER BY daysLeft ASC")
    suspend fun getExpiringSoonItems(): List<GroceryItem>

    @Query("DELETE FROM grocery_items WHERE daysLeft < 0")
    suspend fun deleteExpiredItems(): Int

    @Query("SELECT COUNT(*) FROM grocery_items")
    suspend fun getTotalItemsCount(): Int

    @Query("SELECT COUNT(*) FROM grocery_items WHERE daysLeft <= 3")
    suspend fun getExpiringSoonCount(): Int

    @Query("SELECT COUNT(*) FROM grocery_items WHERE daysLeft < 0")
    suspend fun getExpiredCount(): Int

    @Query("SELECT category, COUNT(*) as count FROM grocery_items GROUP BY category")
    suspend fun getCategoryBreakdown(): List<CategoryCount>

    @Query("SELECT category, COUNT(*) as itemCount FROM grocery_items GROUP BY category ORDER BY itemCount DESC")
    suspend fun getCategorySummary(): List<CategorySummary>
}

data class CategoryCount(
    val category: String,
    val count: Int
)

data class CategorySummary(
    val category: String,
    val itemCount: Int
)
