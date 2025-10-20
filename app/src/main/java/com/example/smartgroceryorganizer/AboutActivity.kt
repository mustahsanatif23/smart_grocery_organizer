package com.example.smartgroceryorganizer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.smartgroceryorganizer.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupAppInfo()
        setupListeners()
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
        supportActionBar?.title = getString(R.string.nav_about)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupAppInfo() {
        // Set app version from BuildConfig
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            binding.tvVersionValue.text = packageInfo.versionName
            binding.tvBuildValue.text = packageInfo.versionCode.toString()
        } catch (e: Exception) {
            binding.tvVersionValue.text = "1.0"
            binding.tvBuildValue.text = "1"
        }

        // Set app name and description
        binding.tvAppName.text = getString(R.string.app_name)
        binding.tvAppDescription.text = getString(R.string.app_description)

        // Set developer info
        binding.tvDeveloperValue.text = "Mustahsan Atif"
        binding.tvEmailValue.text = "support@smartgrocery.com"

        // Set license info
        binding.tvLicenseValue.text = "MIT License"
    }

    private fun setupListeners() {
        // Rate app
        binding.cardRateApp.setOnClickListener {
            rateApp()
        }

        // Share app
        binding.cardShareApp.setOnClickListener {
            shareApp()
        }

        // Privacy Policy
        binding.cardPrivacyPolicy.setOnClickListener {
            openPrivacyPolicy()
        }

        // Terms of Service
        binding.cardTermsOfService.setOnClickListener {
            openTermsOfService()
        }

        // Send Feedback
        binding.cardFeedback.setOnClickListener {
            sendFeedback()
        }

        // Open Source Licenses
        binding.cardOpenSource.setOnClickListener {
            openSourceLicenses()
        }

        // Visit Website
        binding.btnVisitWebsite.setOnClickListener {
            openWebsite()
        }

        // Contact Support
        binding.btnContactSupport.setOnClickListener {
            contactSupport()
        }
    }

    private fun rateApp() {
        try {
            val uri = Uri.parse("market://details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.play_store_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }

    private fun openPrivacyPolicy() {
        openUrl("https://smartgrocery.com/privacy")
    }

    private fun openTermsOfService() {
        openUrl("https://smartgrocery.com/terms")
    }

    private fun openWebsite() {
        openUrl("https://smartgrocery.com")
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.browser_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendFeedback() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@smartgrocery.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for ${getString(R.string.app_name)}")
                putExtra(Intent.EXTRA_TEXT, "")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.email_client_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    private fun contactSupport() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@smartgrocery.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Support Request - ${getString(R.string.app_name)}")
                putExtra(Intent.EXTRA_TEXT, "")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.email_client_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSourceLicenses() {
        val licenses = """
            ${getString(R.string.app_name)} uses the following open source libraries:
            
            • AndroidX Libraries - Apache 2.0 License
            • Material Components - Apache 2.0 License
            • Room Database - Apache 2.0 License
            • Kotlin - Apache 2.0 License
            • Firebase - Apache 2.0 License
            • WorkManager - Apache 2.0 License
            
            Full license texts available at:
            https://smartgrocery.com/licenses
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.open_source_licenses))
            .setMessage(licenses)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }
}
