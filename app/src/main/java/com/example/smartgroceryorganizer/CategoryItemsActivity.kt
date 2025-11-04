package com.example.smartgroceryorganizer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartgroceryorganizer.databinding.ActivityCategoryItemsBinding

class CategoryItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryItemsBinding
    private lateinit var adapter: GroceryAdapter
    private val viewModel: GroceryViewModel by viewModels()
    private lateinit var categoryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryName = intent.getStringExtra("category_name") ?: "Unknown"

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun applySavedTheme() {
        val sharedPreferences = getSharedPreferences(
            SettingsActivity.PREFS_NAME,
            MODE_PRIVATE
        )
        val themeMode = sharedPreferences.getInt(
            SettingsActivity.KEY_THEME_MODE,
            AppCompatDelegate.MODE_NIGHT_NO
        )
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = categoryName
        supportActionBar?.subtitle = "View all items in this category"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = GroceryAdapter(
            onClick = { item -> openItemDetails(item) },
            onStarToggle = { item ->
                viewModel.toggleItemUrgency(item)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getExpiryWarningDays(): Int {
        val sharedPreferences = getSharedPreferences(
            "SmartGroceryOrganizerPrefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getInt("expiry_warning_days", 3)
    }

    private fun observeViewModel() {
        viewModel.groceryItems.observe(this, Observer { allItems ->
            // Filter items by category
            val categoryItems = allItems.filter {
                it.category.equals(categoryName, ignoreCase = true)
            }

            if (categoryItems.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(categoryItems)
            }

            // Update stats using user's expiry warning setting
            binding.tvTotalItems.text = categoryItems.size.toString()
            val expiryWarningDays = getExpiryWarningDays()
            val expiringSoon = categoryItems.count { it.daysLeft <= expiryWarningDays }
            binding.tvExpiringSoon.text = expiringSoon.toString()

            // Show/hide alert card
            if (expiringSoon > 0) {
                binding.cardAlert.visibility = View.VISIBLE
                binding.tvAlert.text = "$expiringSoon items in $categoryName expiring soon"
            } else {
                binding.cardAlert.visibility = View.GONE
            }
        })
    }

    private fun openItemDetails(item: GroceryItem) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("item_id", item.id)
            putExtra("item_name", item.name)
            putExtra("item_category", item.category)
            putExtra("item_quantity", item.quantity)
            putExtra("item_expiry", item.expiry)
            putExtra("item_days", item.daysLeft)
            putExtra("item_urgent", item.urgent)
        }
        startActivity(intent)
    }
}
