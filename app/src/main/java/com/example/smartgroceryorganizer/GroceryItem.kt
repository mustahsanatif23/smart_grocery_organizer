package com.example.smartgroceryorganizer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val quantity: String,
    val expiry: String,
    val daysLeft: Int,
    val urgent: Boolean
)
