package com.example.smartgroceryorganizer

enum class SortOption(val displayName: String) {
    EXPIRY_EARLIEST("Expiry: Earliest First"),
    EXPIRY_LATEST("Expiry: Latest First"),
    NAME_A_TO_Z("Name: A-Z"),
    NAME_Z_TO_A("Name: Z-A"),
    CATEGORY_A_TO_Z("Category: A-Z"),
    CATEGORY_Z_TO_A("Category: Z-A"),
    URGENT_FIRST("Urgent Items First")
}
