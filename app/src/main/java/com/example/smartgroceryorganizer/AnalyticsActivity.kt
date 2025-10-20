package com.example.smartgroceryorganizer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartgroceryorganizer.databinding.ActivityAnalyticsBinding

class AnalyticsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAnalyticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = "Analytics"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}
