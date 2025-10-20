package com.example.smartgroceryorganizer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import com.example.smartgroceryorganizer.databinding.ActivityCategoriesBinding

class CategoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var adapter: CategoryAdapter
    private val viewModel: GroceryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun applySavedTheme() {
        val sharedPreferences = getSharedPreferences(
            SettingsActivity.PREFS_NAME,
            android.content.Context.MODE_PRIVATE
        )
        val themeMode = sharedPreferences.getInt(
            SettingsActivity.KEY_THEME_MODE,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.nav_categories)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter { categoryData ->
            openCategoryItems(categoryData)
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.groceryItems.observe(this) { items ->
            if (items.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                updateCategoryData(items)
            }
        }
    }

    private fun updateCategoryData(items: List<GroceryItem>) {
        // Get categories from string resources
        val categories = resources.getStringArray(R.array.grocery_categories)

        // Create category data with item counts
        val categoryDataList = categories.map { category ->
            val itemCount = items.count { it.category.equals(category, ignoreCase = true) }
            val expiringSoonCount = items.count {
                it.category.equals(category, ignoreCase = true) && (it.daysLeft <= 2 || it.urgent)
            }
            CategoryData(
                name = category,
                itemCount = itemCount,
                expiringSoonCount = expiringSoonCount,
                icon = getCategoryIcon(category)
            )
        }.filter { it.itemCount > 0 } // Only show categories with items

        adapter.submitList(categoryDataList)

        // Update total category count
        binding.tvCategoryCount.text = getString(R.string.categories_count, categoryDataList.size)
    }

    private fun getCategoryIcon(category: String): Int {
        return when (category.lowercase()) {
            "fruits" -> R.drawable.ic_category
            "vegetables" -> R.drawable.ic_category
            "dairy" -> R.drawable.ic_category
            "meat" -> R.drawable.ic_category
            "grains" -> R.drawable.ic_category
            "bakery" -> R.drawable.ic_category
            "beverages" -> R.drawable.ic_category
            "snacks" -> R.drawable.ic_category
            "frozen foods" -> R.drawable.ic_category
            "canned goods" -> R.drawable.ic_category
            "condiments" -> R.drawable.ic_category
            else -> R.drawable.ic_category
        }
    }

    private fun openCategoryItems(categoryData: CategoryData) {
        val intent = Intent(this, CategoryItemsActivity::class.java).apply {
            putExtra("category_name", categoryData.name)
            putExtra("item_count", categoryData.itemCount)
        }
        startActivity(intent)
    }
}

data class CategoryData(
    val name: String,
    val itemCount: Int,
    val expiringSoonCount: Int,
    val icon: Int
)
