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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: GroceryAdapter
    private val viewModel: GroceryViewModel by viewModels()

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
        setupBackPressHandler()
        requestNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_home
        viewModel.refreshData()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_home
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
            initializeNotifications()
        }
    }

    private fun initializeNotifications() {
        NotificationHelper.createNotificationChannel(this)
        NotificationScheduler.scheduleExpiryNotifications(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
                R.id.nav_bottom_home -> true
                R.id.nav_bottom_recipes -> {
                    navigateToActivity(RecipesActivity::class.java, "right")
                    true
                }
                R.id.nav_bottom_analytics -> {
                    navigateToActivity(AnalyticsActivity::class.java, "right")
                    true
                }
                R.id.nav_bottom_settings -> {
                    navigateToActivity(SettingsActivity::class.java, "right")
                    true
                }
                else -> false
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun navigateToActivity(activityClass: Class<*>, direction: String = "right") {
        val intent = Intent(this, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)

        when (direction) {
            "right" -> overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            "left" -> overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            else -> overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
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
        binding.btnSort?.setOnClickListener {
            showSortDialog()
        }
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
        viewModel.groceryItems.observe(this) { items ->
            adapter.submitList(items)
        }

        viewModel.currentSortOption.observe(this) { sortOption ->
            binding.tvCurrentSort?.text = getString(R.string.sorted_by, sortOption.displayName)
        }

        viewModel.itemCount.observe(this) { count ->
            binding.tvItemCount.text = count.toString()
        }

        viewModel.expiringSoonCount.observe(this) { count ->
            binding.tvExpiringSoon.text = count.toString()
            binding.cardAlert.visibility = View.GONE
        }

        viewModel.isEmpty.observe(this) { isEmpty ->
            if (isEmpty) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
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
}

