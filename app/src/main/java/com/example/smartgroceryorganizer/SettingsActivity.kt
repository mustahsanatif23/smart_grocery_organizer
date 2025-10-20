package com.example.smartgroceryorganizer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.smartgroceryorganizer.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREFS_NAME = "SmartGroceryOrganizerPrefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_NOTIFICATION_TIME = "notification_time"
        const val KEY_AUTO_DELETE_EXPIRED = "auto_delete_expired"
        const val KEY_EXPIRY_WARNING_DAYS = "expiry_warning_days"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved theme before setting content view
        applySavedTheme()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupToolbar()
        loadSettings()
        setupListeners()
    }

    private fun applySavedTheme() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.nav_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadSettings() {
        // Load theme setting
        val themeMode = sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        binding.switchTheme.isChecked = themeMode == AppCompatDelegate.MODE_NIGHT_YES

        // Load notification settings
        binding.switchNotifications.isChecked = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        binding.tvNotificationTime.text = sharedPreferences.getString(KEY_NOTIFICATION_TIME, "12:00 PM")

        // Load auto-delete setting
        binding.switchAutoDelete.isChecked = sharedPreferences.getBoolean(KEY_AUTO_DELETE_EXPIRED, false)

        // Load expiry warning days
        val warningDays = sharedPreferences.getInt(KEY_EXPIRY_WARNING_DAYS, 3)
        binding.tvExpiryWarningDays.text = "$warningDays days"

        // Load account settings
        binding.tvUsername.text = sharedPreferences.getString(KEY_USERNAME, "Not set")
        binding.tvEmail.text = sharedPreferences.getString(KEY_EMAIL, "Not set")

        // Update notification options visibility
        updateNotificationOptionsVisibility()
    }

    private fun setupListeners() {
        // Theme toggle
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            val newThemeMode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            saveThemePreference(newThemeMode)
            AppCompatDelegate.setDefaultNightMode(newThemeMode)
            Toast.makeText(this, getString(R.string.theme_updated), Toast.LENGTH_SHORT).show()
        }

        // Notifications toggle
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked).apply()
            updateNotificationOptionsVisibility()

            if (isChecked) {
                NotificationScheduler.scheduleExpiryNotifications(this)
                Toast.makeText(this, getString(R.string.notifications_enabled), Toast.LENGTH_SHORT).show()
            } else {
                NotificationScheduler.cancelExpiryNotifications(this)
                Toast.makeText(this, getString(R.string.notifications_disabled), Toast.LENGTH_SHORT).show()
            }
        }

        // Notification time picker
        binding.cardNotificationTime.setOnClickListener {
            if (binding.switchNotifications.isChecked) {
                showNotificationTimePicker()
            }
        }

        // Auto-delete toggle
        binding.switchAutoDelete.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(KEY_AUTO_DELETE_EXPIRED, isChecked).apply()
            Toast.makeText(
                this,
                if (isChecked) getString(R.string.auto_delete_enabled) else getString(R.string.auto_delete_disabled),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Expiry warning days
        binding.cardExpiryWarning.setOnClickListener {
            showExpiryWarningDialog()
        }

        // Account settings
        binding.cardUsername.setOnClickListener {
            showEditDialog(
                getString(R.string.edit_username),
                binding.tvUsername.text.toString(),
                KEY_USERNAME
            ) { newValue ->
                binding.tvUsername.text = newValue
            }
        }

        binding.cardEmail.setOnClickListener {
            showEditDialog(
                getString(R.string.edit_email),
                binding.tvEmail.text.toString(),
                KEY_EMAIL
            ) { newValue ->
                binding.tvEmail.text = newValue
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

        // About
        binding.cardAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun updateNotificationOptionsVisibility() {
        val enabled = binding.switchNotifications.isChecked
        binding.cardNotificationTime.isEnabled = enabled
        binding.cardNotificationTime.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun saveThemePreference(themeMode: Int) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, themeMode).apply()
    }

    private fun showNotificationTimePicker() {
        val times = arrayOf("8:00 AM", "10:00 AM", "12:00 PM", "2:00 PM", "4:00 PM", "6:00 PM", "8:00 PM")
        val currentTime = sharedPreferences.getString(KEY_NOTIFICATION_TIME, "12:00 PM")
        val currentIndex = times.indexOf(currentTime)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_notification_time))
            .setSingleChoiceItems(times, currentIndex) { dialog, which ->
                val selectedTime = times[which]
                sharedPreferences.edit().putString(KEY_NOTIFICATION_TIME, selectedTime).apply()
                binding.tvNotificationTime.text = selectedTime
                NotificationScheduler.scheduleExpiryNotifications(this)
                Toast.makeText(this, getString(R.string.notification_time_updated), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showExpiryWarningDialog() {
        val days = arrayOf("1 day", "2 days", "3 days", "5 days", "7 days")
        val daysValues = arrayOf(1, 2, 3, 5, 7)
        val currentDays = sharedPreferences.getInt(KEY_EXPIRY_WARNING_DAYS, 3)
        val currentIndex = daysValues.indexOf(currentDays)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.expiry_warning_days))
            .setSingleChoiceItems(days, currentIndex) { dialog, which ->
                val selectedDays = daysValues[which]
                sharedPreferences.edit().putInt(KEY_EXPIRY_WARNING_DAYS, selectedDays).apply()
                binding.tvExpiryWarningDays.text = days[which]
                Toast.makeText(this, getString(R.string.expiry_warning_updated), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showEditDialog(title: String, currentValue: String, key: String, onSave: (String) -> Unit) {
        val editText = android.widget.EditText(this)
        editText.setText(if (currentValue == "Not set") "" else currentValue)
        editText.setPadding(50, 30, 50, 30)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newValue = editText.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    sharedPreferences.edit().putString(key, newValue).apply()
                    onSave(newValue)
                    Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
                }
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
        // This would clear all grocery items from the database
        Toast.makeText(this, getString(R.string.data_cleared), Toast.LENGTH_SHORT).show()
        // In a real implementation, call viewModel or repository to clear database
    }

    private fun resetAllSettings() {
        sharedPreferences.edit().clear().apply()
        loadSettings()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        Toast.makeText(this, getString(R.string.settings_reset), Toast.LENGTH_SHORT).show()
    }
}

