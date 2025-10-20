package com.example.smartgroceryorganizer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smartgroceryorganizer.databinding.ActivityRecipesBinding

class RecipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = "Recipe Suggestions"
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Sample static recipes (in a real app read from DB or JSON)
        binding.tvHint.text = "Recipe suggestions are based on available groceries."
    }
}
