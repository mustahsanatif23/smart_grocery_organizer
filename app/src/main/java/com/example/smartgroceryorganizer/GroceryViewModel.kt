package com.example.smartgroceryorganizer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GroceryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GroceryRepository
    private val allItemsFromDb: LiveData<List<GroceryItem>>

    // LiveData for current sort option
    private val _currentSortOption = MutableLiveData<SortOption>(SortOption.EXPIRY_EARLIEST)
    val currentSortOption: LiveData<SortOption> = _currentSortOption

    // Sorted grocery items based on current sort option
    val groceryItems: LiveData<List<GroceryItem>> = _currentSortOption.switchMap { sortOption ->
        allItemsFromDb.map { items ->
            sortItems(items, sortOption)
        }
    }

    // LiveData for item count
    private val _itemCount = MutableLiveData<Int>()
    val itemCount: LiveData<Int> = _itemCount

    // LiveData for expiring soon count
    private val _expiringSoonCount = MutableLiveData<Int>()
    val expiringSoonCount: LiveData<Int> = _expiringSoonCount

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for empty state
    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    // LiveData for search/filter mode
    private val _searchQuery = MutableLiveData<String>()
    private val _filterCategory = MutableLiveData<String>()

    init {
        val groceryDao = GroceryDatabase.getDatabase(application).groceryDao()
        repository = GroceryRepository(groceryDao)
        allItemsFromDb = repository.allItems

        // Observe items and update counts
        groceryItems.observeForever { items ->
            updateCounts(items)
        }
    }

    /**
     * Sort items based on the selected option
     */
    private fun sortItems(items: List<GroceryItem>, sortOption: SortOption): List<GroceryItem> {
        return when (sortOption) {
            SortOption.EXPIRY_EARLIEST -> items.sortedBy { it.daysLeft }
            SortOption.EXPIRY_LATEST -> items.sortedByDescending { it.daysLeft }
            SortOption.NAME_A_TO_Z -> items.sortedBy { it.name.lowercase() }
            SortOption.NAME_Z_TO_A -> items.sortedByDescending { it.name.lowercase() }
            SortOption.CATEGORY_A_TO_Z -> items.sortedBy { it.category.lowercase() }
            SortOption.CATEGORY_Z_TO_A -> items.sortedByDescending { it.category.lowercase() }
            SortOption.URGENT_FIRST -> items.sortedWith(compareByDescending<GroceryItem> { it.urgent }.thenBy { it.daysLeft })
        }
    }

    /**
     * Update the current sort option
     */
    fun setSortOption(sortOption: SortOption) {
        _currentSortOption.value = sortOption
    }

    /**
     * Toggle urgency status for a single item
     */
    fun toggleItemUrgency(item: GroceryItem) = viewModelScope.launch {
        val updatedItem = item.copy(urgent = !item.urgent)
        repository.update(updatedItem)
    }

    /**
     * Mark multiple items as urgent
     */
    fun markItemsAsUrgent(items: List<GroceryItem>, isUrgent: Boolean) = viewModelScope.launch {
        items.forEach { item ->
            val updatedItem = item.copy(urgent = isUrgent)
            repository.update(updatedItem)
        }
    }

    /**
     * Get all available sort options
     */
    fun getAllSortOptions(): List<SortOption> {
        return SortOption.values().toList()
    }

    /**
     * Add a new grocery item
     */
    fun addItem(item: GroceryItem) = viewModelScope.launch {
        repository.insert(item)
    }

    /**
     * Update an existing grocery item
     */
    fun updateItem(item: GroceryItem) = viewModelScope.launch {
        repository.update(item)
    }

    /**
     * Remove a grocery item
     */
    fun removeItem(item: GroceryItem) = viewModelScope.launch {
        repository.delete(item)
    }

    /**
     * Remove a grocery item by ID
     */
    fun removeItemById(itemId: Int) = viewModelScope.launch {
        repository.deleteById(itemId)
    }

    /**
     * Refresh data
     */
    fun refreshData() {
        // Data is automatically refreshed via LiveData
        _isLoading.value = true
        _isLoading.value = false
    }

    /**
     * Filter items by category
     */
    fun filterByCategory(category: String) {
        _filterCategory.value = category
    }

    /**
     * Search items by name or category
     */
    fun searchItems(query: String) {
        _searchQuery.value = query
    }

    /**
     * Clear search and show all items
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _filterCategory.value = ""
    }

    /**
     * Update counts based on current items
     */
    private fun updateCounts(items: List<GroceryItem>?) {
        val itemList = items ?: emptyList()
        _itemCount.value = itemList.size
        // Count items expiring soon (2 days or fewer) OR marked as urgent
        _expiringSoonCount.value = itemList.count { it.daysLeft <= 2 || it.urgent }
        _isEmpty.value = itemList.isEmpty()
    }

    /**
     * Get item by ID
     */
    suspend fun getItemById(itemId: Int): GroceryItem? {
        return repository.getItemById(itemId)
    }
}

