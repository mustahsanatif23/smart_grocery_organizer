package com.example.smartgroceryorganizer

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.smartgroceryorganizer.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREFS_NAME = "SmartGroceryOrganizerPrefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_AUTO_DELETE_EXPIRED = "auto_delete_expired"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        applySavedTheme()
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadSettings()
        setupListeners()
        setupBottomNavigation()
        setupBackPressHandler()
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_settings
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_settings
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })
    }

    @Suppress("DEPRECATION")
    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_settings

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bottom_home -> {
                    navigateToActivity(MainActivity::class.java, "left")
                    true
                }
                R.id.nav_bottom_recipes -> {
                    navigateToActivity(RecipesActivity::class.java, "left")
                    true
                }
                R.id.nav_bottom_analytics -> {
                    navigateToActivity(AnalyticsActivity::class.java, "left")
                    true
                }
                R.id.nav_bottom_settings -> true
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
        finish()
    }

    private fun applySavedTheme() {
        val themeMode = sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.nav_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { navigateToHome() }
    }

    private fun loadSettings() {
        val notSetText = getString(R.string.not_set)

        val themeMode = sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO)
        binding.switchTheme.isChecked = themeMode == AppCompatDelegate.MODE_NIGHT_YES

        val notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        binding.switchNotifications.isChecked = notificationsEnabled

        val notificationHour = sharedPreferences.getInt("notification_hour", 12)
        binding.tvNotificationTime.text = String.format(Locale.getDefault(), "%02d:00", notificationHour)

        val autoDeleteEnabled = sharedPreferences.getBoolean(KEY_AUTO_DELETE_EXPIRED, false)
        binding.switchAutoDelete.isChecked = autoDeleteEnabled

        val expiryWarningDays = sharedPreferences.getInt("expiry_warning_days", 3)
        binding.tvExpiryWarningDays.text = resources.getQuantityString(R.plurals.days_count, expiryWarningDays, expiryWarningDays)

        val username = sharedPreferences.getString(KEY_USERNAME, null)
        binding.tvUsername.text = if (username.isNullOrBlank()) notSetText else username

        val email = sharedPreferences.getString(KEY_EMAIL, null)
        binding.tvEmail.text = if (email.isNullOrBlank()) notSetText else email
    }

    private fun setupListeners() {
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            val newThemeMode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            saveThemePreference(newThemeMode)
            AppCompatDelegate.setDefaultNightMode(newThemeMode)
            recreate()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit {
                putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked)
            }

            if (isChecked) {
                NotificationScheduler.scheduleExpiryNotifications(this)
                Toast.makeText(this, getString(R.string.notifications_enabled), Toast.LENGTH_SHORT).show()
            } else {
                NotificationScheduler.cancelExpiryNotifications(this)
                Toast.makeText(this, getString(R.string.notifications_disabled), Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchAutoDelete.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit {
                putBoolean(KEY_AUTO_DELETE_EXPIRED, isChecked)
            }
            Toast.makeText(
                this,
                if (isChecked) getString(R.string.auto_delete_enabled) else getString(R.string.auto_delete_disabled),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Account settings
        binding.cardUsername.setOnClickListener {
            showEditDialog(
                getString(R.string.edit_username),
                binding.tvUsername.text.toString(),
                KEY_USERNAME
            ) { newValue ->
                binding.tvUsername.text = newValue.ifBlank { getString(R.string.not_set) }
            }
        }

        binding.cardEmail.setOnClickListener {
            showEditDialog(
                getString(R.string.edit_email),
                binding.tvEmail.text.toString(),
                KEY_EMAIL
            ) { newValue ->
                binding.tvEmail.text = newValue.ifBlank { getString(R.string.not_set) }
            }
        }

        // Clear data
        binding.btnClearData.setOnClickListener {
            showClearDataConfirmation()
        }

        // Reset settings
        binding.btnResetSettings.setOnClickListener {
            showResetSettingsConfirmation()
        }

        // Notification time settings
        binding.cardNotificationTime.setOnClickListener {
            showNotificationTimeDialog()
        }

        // Expiry warning days
        binding.cardExpiryWarning.setOnClickListener {
            showExpiryWarningDialog()
        }

        // About
        binding.cardAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun saveThemePreference(themeMode: Int) {
        sharedPreferences.edit {
            putInt(KEY_THEME_MODE, themeMode)
        }
    }

    private fun showEditDialog(title: String, currentValue: String, key: String, onSave: (String) -> Unit) {
        val editText = EditText(this)
        val notSetText = getString(R.string.not_set)
        editText.setText(if (currentValue == notSetText) "" else currentValue)
        editText.setPadding(50, 30, 50, 30)

        // Set input type based on the field
        if (key == KEY_EMAIL) {
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newValue = editText.text.toString().trim()

                // If empty, treat as clearing the value (show 'Not set')
                if (newValue.isEmpty()) {
                    sharedPreferences.edit {
                        remove(key)
                    }
                    onSave("")
                    Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Additional validation for email when provided
                if (key == KEY_EMAIL && !Patterns.EMAIL_ADDRESS.matcher(newValue).matches()) {
                    Toast.makeText(this, getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                sharedPreferences.edit {
                    putString(key, newValue)
                }
                onSave(newValue)
                Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showClearDataConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_data))
            .setMessage(getString(R.string.clear_data_confirmation))
            .setPositiveButton(getString(R.string.clear)) { _, _ ->
                clearAllData()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showResetSettingsConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reset_settings))
            .setMessage(getString(R.string.reset_settings_confirmation))
            .setPositiveButton(getString(R.string.reset)) { _, _ ->
                resetAllSettings()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun clearAllData() {
        // Clear all grocery items from the database
        lifecycleScope.launch {
            try {
                val database = GroceryDatabase.getDatabase(applicationContext)
                database.groceryDao().deleteAllItems()
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, getString(R.string.data_cleared), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "Error clearing data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resetAllSettings() {
        sharedPreferences.edit {
            clear()
        }
        // Reload settings to defaults
        loadSettings()
        // Reset theme to light mode and apply immediately
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Toast.makeText(this, getString(R.string.settings_reset), Toast.LENGTH_SHORT).show()
        // Recreate activity to ensure theme/apply changes are visible
        recreate()
    }

    private fun showNotificationTimeDialog() {
        val hours = Array(24) { i -> String.format(Locale.getDefault(), "%02d:00", i) }
        val currentHour = sharedPreferences.getInt("notification_hour", 12)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.notification_time))
            .setSingleChoiceItems(hours, currentHour) { dialog, which ->
                sharedPreferences.edit {
                    putInt("notification_hour", which)
                }
                binding.tvNotificationTime.text = hours[which]
                NotificationScheduler.scheduleExpiryNotifications(this)
                Toast.makeText(this, getString(R.string.notification_time_updated), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showExpiryWarningDialog() {
        val warningDays = arrayOf("1 day", "2 days", "3 days", "5 days", "7 days")
        val warningValues = arrayOf(1, 2, 3, 5, 7)
        val currentValue = sharedPreferences.getInt("expiry_warning_days", 3)
        val currentIndex = warningValues.indexOf(currentValue).takeIf { it >= 0 } ?: 2

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.expiry_warning))
            .setSingleChoiceItems(warningDays, currentIndex) { dialog, which ->
                sharedPreferences.edit {
                    putInt("expiry_warning_days", warningValues[which])
                }
                binding.tvExpiryWarningDays.text = warningDays[which]
                Toast.makeText(this, getString(R.string.expiry_warning_updated), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}

