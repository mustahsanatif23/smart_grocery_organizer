package com.example.smartgroceryorganizer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [GroceryItem::class], version = 1, exportSchema = false)
abstract class GroceryDatabase : RoomDatabase() {

    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: GroceryDatabase? = null

        fun getDatabase(context: Context): GroceryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GroceryDatabase::class.java,
                    "grocery_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate database with initial data
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.groceryDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(groceryDao: GroceryDao) {
            // Add some sample data
            groceryDao.insertItem(GroceryItem(name = "Milk", category = "Dairy", quantity = "1L", expiry = "2025-10-25", daysLeft = 5, urgent = false))
            groceryDao.insertItem(GroceryItem(name = "Eggs", category = "Dairy", quantity = "12 pcs", expiry = "2025-10-23", daysLeft = 3, urgent = true))
            groceryDao.insertItem(GroceryItem(name = "Tomatoes", category = "Vegetables", quantity = "500g", expiry = "2025-10-22", daysLeft = 2, urgent = true))
            groceryDao.insertItem(GroceryItem(name = "Chicken Breast", category = "Meat", quantity = "1kg", expiry = "2025-10-24", daysLeft = 4, urgent = false))
            groceryDao.insertItem(GroceryItem(name = "Bread", category = "Bakery", quantity = "1 loaf", expiry = "2025-10-26", daysLeft = 6, urgent = false))
            groceryDao.insertItem(GroceryItem(name = "Orange Juice", category = "Beverages", quantity = "1L", expiry = "2025-10-28", daysLeft = 8, urgent = false))
            groceryDao.insertItem(GroceryItem(name = "Yogurt", category = "Dairy", quantity = "500g", expiry = "2025-10-21", daysLeft = 1, urgent = true))
            groceryDao.insertItem(GroceryItem(name = "Carrots", category = "Vegetables", quantity = "1kg", expiry = "2025-10-27", daysLeft = 7, urgent = false))
        }
    }
}

