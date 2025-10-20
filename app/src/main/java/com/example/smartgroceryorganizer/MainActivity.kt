package com.example.smartgroceryorganizer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartgroceryorganizer.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: GroceryAdapter
    private val viewModel: GroceryViewModel by viewModels()

    // Notification permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            initializeNotifications()
        } else {
            Toast.makeText(this, "Notification permission denied. You won't receive expiry alerts.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before calling super.onCreate()
        applySavedTheme()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigationDrawer()
        setupBottomNavigation()
        setupRecyclerView()
        setupFab()
        setupSwipeRefresh()
        setupSearch()
        observeViewModel()

        // Initialize notification system
        requestNotificationPermission()
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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    initializeNotifications()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android versions below 13, no runtime permission needed
            initializeNotifications()
        }
    }

    private fun initializeNotifications() {
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Schedule daily notifications at 12 PM
        NotificationScheduler.scheduleExpiryNotifications(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, getString(R.string.already_on_home), Toast.LENGTH_SHORT).show()
                }
                R.id.nav_categories -> {
                    startActivity(Intent(this, CategoriesActivity::class.java))
                }
                R.id.nav_recipes -> {
                    startActivity(Intent(this, RecipesActivity::class.java))
                }
                R.id.nav_analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bottom_home -> {
                    Toast.makeText(this, getString(R.string.already_on_home), Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_bottom_recipes -> {
                    startActivity(Intent(this, RecipesActivity::class.java))
                    true
                }
                R.id.nav_bottom_analytics -> {
                    startActivity(Intent(this, AnalyticsActivity::class.java))
                    true
                }
                R.id.nav_bottom_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = GroceryAdapter(
            onClick = { item -> openItemDetails(item) },
            onStarToggle = { item ->
                viewModel.toggleItemUrgency(item)
                Toast.makeText(this,
                    if (item.urgent) "Removed from urgent" else "Marked as urgent",
                    Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditActivity::class.java))
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
            binding.swipeRefresh.isRefreshing = false
            Toast.makeText(this, getString(R.string.data_refreshed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            showSearchDialog()
        }

        // Setup sorting button
        binding.btnSort?.setOnClickListener {
            showSortDialog()
        }
    }

    private fun showSearchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search, null)
        val searchInput = dialogView.findViewById<TextInputEditText>(R.id.etSearch)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.action_search))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.action_search)) { _, _ ->
                val query = searchInput.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchItems(query)
                    Toast.makeText(this, "Searching for: $query", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Clear") { _, _ ->
                viewModel.clearSearch()
                Toast.makeText(this, "Search cleared", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showSortDialog() {
        val sortOptions = viewModel.getAllSortOptions()
        val sortNames = sortOptions.map { it.displayName }.toTypedArray()
        val currentSort = viewModel.currentSortOption.value ?: SortOption.EXPIRY_EARLIEST
        val currentIndex = sortOptions.indexOf(currentSort)

        AlertDialog.Builder(this)
            .setTitle("Sort Items By")
            .setSingleChoiceItems(sortNames, currentIndex) { dialog, which ->
                val selectedOption = sortOptions[which]
                viewModel.setSortOption(selectedOption)
                Toast.makeText(this, getString(R.string.sorted_by, selectedOption.displayName), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        // Observe grocery items
        viewModel.groceryItems.observe(this) { items ->
            adapter.submitList(items)
        }

        // Observe current sort option
        viewModel.currentSortOption.observe(this) { sortOption ->
            binding.tvCurrentSort?.text = getString(R.string.sorted_by, sortOption.displayName)
        }

        // Observe item count
        viewModel.itemCount.observe(this) { count ->
            binding.tvItemCount.text = count.toString()
        }

        // Observe expiring soon count
        viewModel.expiringSoonCount.observe(this) { count ->
            binding.tvExpiringSoon.text = count.toString()

            // Update alert visibility and text
            if (count > 0) {
                binding.cardAlert.visibility = View.VISIBLE
                binding.tvAlert.text = getString(R.string.expiring_soon, count)
            } else {
                binding.cardAlert.visibility = View.GONE
            }
        }

        // Observe empty state
        viewModel.isEmpty.observe(this) { isEmpty ->
            if (isEmpty) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }
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

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
