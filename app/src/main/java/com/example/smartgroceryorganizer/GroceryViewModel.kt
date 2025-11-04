package com.example.smartgroceryorganizer

import android.app.Application
import android.content.Context
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

    private val _currentSortOption = MutableLiveData(SortOption.EXPIRY_EARLIEST)
    val currentSortOption: LiveData<SortOption> = _currentSortOption

    private val _itemCount = MutableLiveData<Int>()
    val itemCount: LiveData<Int> = _itemCount

    private val _expiringSoonCount = MutableLiveData<Int>()
    val expiringSoonCount: LiveData<Int> = _expiringSoonCount

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    init {
        val groceryDao = GroceryDatabase.getDatabase(application).groceryDao()
        repository = GroceryRepository(groceryDao)
        allItemsFromDb = repository.allItems
    }

    /** Sorted grocery items based on current sort option */
    val groceryItems: LiveData<List<GroceryItem>> = _currentSortOption.switchMap { sortOption ->
        allItemsFromDb.map { items ->
            sortItems(items, sortOption)
        }
    }.also { liveData ->
        liveData.observeForever { items ->
            updateCounts(items)
        }
    }

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

    fun setSortOption(sortOption: SortOption) {
        _currentSortOption.value = sortOption
    }

    fun toggleItemUrgency(item: GroceryItem) = viewModelScope.launch {
        val updatedItem = item.copy(urgent = !item.urgent)
        repository.update(updatedItem)
    }

    fun getAllSortOptions(): List<SortOption> {
        return SortOption.entries
    }

    fun addItem(item: GroceryItem) = viewModelScope.launch {
        repository.insert(item)
    }

    fun updateItem(item: GroceryItem) = viewModelScope.launch {
        repository.update(item)
    }

    fun removeItemById(itemId: Int) = viewModelScope.launch {
        repository.deleteById(itemId)
    }

    /** Recalculates counts when settings change (e.g., expiry warning days) */
    fun refreshData() {
        val currentItems = groceryItems.value
        if (currentItems != null) {
            updateCounts(currentItems)
        }
    }

    private fun getExpiryWarningDays(): Int {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            "SmartGroceryOrganizerPrefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getInt("expiry_warning_days", 3)
    }

    private fun updateCounts(items: List<GroceryItem>?) {
        val itemList = items ?: emptyList()
        _itemCount.value = itemList.size
        val expiryWarningDays = getExpiryWarningDays()
        _expiringSoonCount.value = itemList.count { it.daysLeft <= expiryWarningDays }
        _isEmpty.value = itemList.isEmpty()
    }
}
