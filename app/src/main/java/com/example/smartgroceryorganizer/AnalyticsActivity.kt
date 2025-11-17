package com.example.smartgroceryorganizer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartgroceryorganizer.databinding.ActivityAnalyticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalyticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalyticsBinding
    private lateinit var database: GroceryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = GroceryDatabase.getDatabase(this)

        setupToolbar()
        setupBottomNavigation()
        loadAnalyticsData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_analytics

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bottom_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                    } else {
                        @Suppress("DEPRECATION")
                        overridePendingTransition(0, 0)
                    }
                    finish()
                    true
                }
                R.id.nav_bottom_recipes -> {
                    val intent = Intent(this, RecipesActivity::class.java)
                    startActivity(intent)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                    } else {
                        @Suppress("DEPRECATION")
                        overridePendingTransition(0, 0)
                    }
                    finish()
                    true
                }
                R.id.nav_bottom_analytics -> {
                    // Already on this screen
                    true
                }
                R.id.nav_bottom_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                    } else {
                        @Suppress("DEPRECATION")
                        overridePendingTransition(0, 0)
                    }
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun getExpiryWarningDays(): Int {
        val sharedPreferences = getSharedPreferences(
            "SmartGroceryOrganizerPrefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getInt("expiry_warning_days", 3)
    }

    private fun loadAnalyticsData() {
        lifecycleScope.launch {
            try {
                val expiryWarningDays = getExpiryWarningDays()

                /** Query all items to respect dynamic expiry warning setting (1-7 days) */
                val allItems = withContext(Dispatchers.IO) {
                    database.groceryDao().getAllItemsList()
                }

                val totalItems = allItems.size
                val expiringSoon = allItems.count { it.daysLeft in 0..expiryWarningDays }

                // Get cumulative expired count from SharedPreferences
                val sharedPreferences = getSharedPreferences("SmartGroceryOrganizerPrefs", Context.MODE_PRIVATE)
                val totalExpired = sharedPreferences.getInt("total_expired_items", 0)

                // Also count any currently expired items still in database and add them
                val currentExpired = allItems.count { it.daysLeft < 0 }

                // Check if auto-delete is enabled and delete any remaining expired items
                val autoDeleteEnabled = sharedPreferences.getBoolean("auto_delete_expired", true)
                if (autoDeleteEnabled && currentExpired > 0) {
                    withContext(Dispatchers.IO) {
                        val repository = GroceryRepository(database.groceryDao())
                        repository.deleteExpiredItemsWithTracking(applicationContext)
                    }
                }

                binding.tvTotalItems.text = totalItems.toString()
                binding.tvExpiringSoon.text = expiringSoon.toString()
                binding.tvExpired.text = (totalExpired + currentExpired).toString()

                val categoryData = database.groceryDao().getCategorySummary()

                setupBarChart(categoryData)
                setupCategoryBreakdown(categoryData, totalItems)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupBarChart(categoryData: List<CategorySummary>) {
        val barChart = binding.barChart

        if (categoryData.isEmpty()) {
            barChart.visibility = View.GONE
            return
        }

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        categoryData.forEachIndexed { index, category ->
            entries.add(BarEntry(index.toFloat(), category.itemCount.toFloat()))
            labels.add(category.category)
        }

        val dataSet = BarDataSet(entries, "Items per Category")

        // Set colors for bars
        val colors = listOf(
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#FFEB3B"), // Yellow
            Color.parseColor("#795548")  // Brown
        )
        dataSet.colors = colors

        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK

        val barData = BarData(dataSet)
        barData.barWidth = 0.7f

        // Configure chart
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)
        barChart.legend.isEnabled = false
        barChart.animateY(1000)

        // Add extra offset at bottom to prevent label cropping
        barChart.setExtraOffsets(5f, 10f, 5f, 20f)

        // Configure X axis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = labels.size
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.textSize = 11f
        xAxis.labelRotationAngle = -45f
        xAxis.yOffset = 5f
        xAxis.setAvoidFirstLastClipping(true)

        // Configure Y axes
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.LTGRAY
        leftAxis.granularity = 1f
        leftAxis.axisMinimum = 0f
        leftAxis.textSize = 11f

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false

        barChart.invalidate()
    }

    private fun setupCategoryBreakdown(categoryData: List<CategorySummary>, totalItems: Int) {
        val container = binding.categoryBreakdownContainer
        container.removeAllViews()

        if (categoryData.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No items to display"
                textSize = 14f
                setTextColor(getColor(android.R.color.darker_gray))
                setPadding(0, 16, 0, 16)
            }
            container.addView(emptyView)
            return
        }

        categoryData.forEach { category ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_category_breakdown, container, false)

            val tvCategoryName = itemView.findViewById<TextView>(R.id.tvCategoryName)
            val tvCategoryCount = itemView.findViewById<TextView>(R.id.tvCategoryCount)
            val progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
            val tvPercentage = itemView.findViewById<TextView>(R.id.tvPercentage)

            tvCategoryName.text = category.category
            tvCategoryCount.text = "${category.itemCount} items"

            val percentage = if (totalItems > 0) {
                (category.itemCount.toFloat() / totalItems.toFloat() * 100).toInt()
            } else {
                0
            }

            progressBar.max = 100
            progressBar.progress = percentage
            tvPercentage.text = "$percentage%"

            container.addView(itemView)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_analytics
    }
}
