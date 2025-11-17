package com.example.smartgroceryorganizer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.smartgroceryorganizer.api.ApiResult
import com.example.smartgroceryorganizer.api.RecipeRepository
import com.example.smartgroceryorganizer.api.RetrofitClient
import com.example.smartgroceryorganizer.databinding.ActivityRecipeDetailsBinding
import kotlinx.coroutines.launch

class RecipeDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailsBinding
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var groceryViewModel: GroceryViewModel
    private lateinit var ingredientsAdapter: RecipeIngredientsAdapter
    private lateinit var instructionsAdapter: RecipeInstructionsAdapter
    private var recipeId: Int = 0
    private var currentRecipeDetails: com.example.smartgroceryorganizer.api.RecipeDetails? = null
    companion object {
        private const val EXTRA_RECIPE_ID = "recipe_id"
        private const val EXTRA_RECIPE_TITLE = "recipe_title"

        fun newIntent(context: Context, recipeId: Int, recipeTitle: String): Intent {
            return Intent(context, RecipeDetailsActivity::class.java).apply {
                putExtra(EXTRA_RECIPE_ID, recipeId)
                putExtra(EXTRA_RECIPE_TITLE, recipeTitle)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get recipe ID from intent
        recipeId = intent.getIntExtra(EXTRA_RECIPE_ID, 0)
        val recipeTitle = intent.getStringExtra(EXTRA_RECIPE_TITLE) ?: "Recipe Details"

        if (recipeId == 0) {
            Toast.makeText(this, "Invalid recipe ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup toolbar
        binding.toolbar.title = recipeTitle
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Initialize repository
        recipeRepository = RecipeRepository(RetrofitClient.apiService)

        // Initialize ViewModel
        groceryViewModel = ViewModelProvider(this)[GroceryViewModel::class.java]

        // Setup Use Recipe button
        setupUseRecipeButton()

        // Setup RecyclerViews
        setupRecyclerViews()

        // Fetch recipe details
        fetchRecipeDetails()
    }

    private fun setupRecyclerViews() {
        // Ingredients RecyclerView
        ingredientsAdapter = RecipeIngredientsAdapter()
        binding.recyclerViewIngredients.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailsActivity)
            adapter = ingredientsAdapter
        }

        // Instructions RecyclerView
        instructionsAdapter = RecipeInstructionsAdapter()
        binding.recyclerViewInstructions.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailsActivity)
            adapter = instructionsAdapter
        }
    }

    private fun fetchRecipeDetails() {
        showLoading()

        lifecycleScope.launch {
            when (val result = recipeRepository.getRecipeDetails(recipeId)) {
                is ApiResult.Success -> {
                    val recipe = result.data
                    displayRecipeDetails(recipe)
                }
                is ApiResult.Error -> {
                    showError(result.message)
                    Toast.makeText(
                        this@RecipeDetailsActivity,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                is ApiResult.Loading -> {
                    // Already showing loading
                }
            }
        }
    }

    private fun displayRecipeDetails(recipe: com.example.smartgroceryorganizer.api.RecipeDetails) {
        hideLoading()

        // Store recipe details for later use
        currentRecipeDetails = recipe

        // Show Use Recipe button
        // binding.fabUseRecipe.visibility = View.VISIBLE

        // Display recipe image - Always show image with fallback to placeholder
        if (!recipe.image.isNullOrBlank()) {
            Glide.with(this)
                .load(recipe.image)
                .placeholder(R.drawable.ic_recipe_placeholder)
                .error(R.drawable.ic_recipe_placeholder)
                .into(binding.ivRecipeImage)
        } else {
            // Set placeholder directly if no image URL
            binding.ivRecipeImage.setImageResource(R.drawable.ic_recipe_placeholder)
        }

        // Display basic info
        binding.tvRecipeTitle.text = recipe.title

        // Display servings and time
        binding.tvServings.text = getString(R.string.servings_format, recipe.servings)
        binding.tvCookingTime.text = getString(R.string.time_format, recipe.readyInMinutes)

        // Display likes if available
        recipe.aggregateLikes?.let { likes ->
            binding.tvLikes.text = getString(R.string.likes_format, likes)
            binding.tvLikes.visibility = View.VISIBLE
        }

        // Display diet tags
        val dietTags = mutableListOf<String>()
        if (recipe.vegetarian == true) dietTags.add("Vegetarian")
        if (recipe.vegan == true) dietTags.add("Vegan")
        if (recipe.glutenFree == true) dietTags.add("Gluten-Free")
        if (recipe.dairyFree == true) dietTags.add("Dairy-Free")

        if (dietTags.isNotEmpty()) {
            binding.tvDietTags.text = dietTags.joinToString(" • ")
            binding.tvDietTags.visibility = View.VISIBLE
        } else {
            binding.tvDietTags.visibility = View.GONE
        }

        // Display summary (remove HTML tags)
        recipe.summary?.let { summary ->
            val cleanSummary = Html.fromHtml(summary, Html.FROM_HTML_MODE_LEGACY).toString()
            binding.tvSummary.text = cleanSummary
            binding.tvSummary.visibility = View.VISIBLE
            binding.tvSummaryLabel.visibility = View.VISIBLE
        }

        // Display ingredients
        if (recipe.extendedIngredients.isNotEmpty()) {
            ingredientsAdapter.submitList(recipe.extendedIngredients)
            binding.recyclerViewIngredients.visibility = View.VISIBLE
            binding.tvIngredientsLabel.visibility = View.VISIBLE
        }

        // Display instructions
        val instructions = recipe.analyzedInstructions
        val tvPlainInstructions = findViewById<TextView>(R.id.tvPlainInstructions)
        val tvNoInstructions = findViewById<View>(R.id.tvNoInstructions)

        if (!instructions.isNullOrEmpty() && instructions.isNotEmpty() && instructions[0].steps.isNotEmpty()) {
            // We have structured instructions with steps
            instructionsAdapter.submitList(instructions[0].steps)
            binding.recyclerViewInstructions.visibility = View.VISIBLE
            binding.tvInstructionsLabel.visibility = View.VISIBLE
            tvNoInstructions.visibility = View.GONE
            tvPlainInstructions.visibility = View.GONE
        } else if (!recipe.instructions.isNullOrBlank()) {
            // Fallback to plain text instructions if analyzed instructions not available
            val cleanInstructions = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_LEGACY
            ).toString()
            // Display plain text instructions in a TextView
            tvPlainInstructions.text = cleanInstructions
            tvPlainInstructions.visibility = View.VISIBLE
            binding.tvInstructionsLabel.visibility = View.VISIBLE
            binding.recyclerViewInstructions.visibility = View.GONE
            tvNoInstructions.visibility = View.GONE
        } else {
            // No instructions available at all - show fallback message
            binding.tvInstructionsLabel.visibility = View.VISIBLE
            tvNoInstructions.visibility = View.VISIBLE
            binding.recyclerViewInstructions.visibility = View.GONE
            tvPlainInstructions.visibility = View.GONE
        }

        // Show content
        binding.scrollView.visibility = View.VISIBLE
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.scrollView.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message

        binding.btnRetry.setOnClickListener {
            fetchRecipeDetails()
        }
    }

    private fun setupUseRecipeButton() {
        // TODO: Add FAB to layout or use alternative button
        // binding.fabUseRecipe.setOnClickListener {
        //     showUseRecipeConfirmation()
        // }
    }

    private fun showUseRecipeConfirmation() {
        val recipe = currentRecipeDetails ?: return

        AlertDialog.Builder(this)
            .setTitle("Use Recipe")
            .setMessage("This will deduct the recipe ingredients from your grocery list. Do you want to continue?")
            .setPositiveButton("Yes") { _, _ ->
                useRecipe(recipe)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun useRecipe(recipe: com.example.smartgroceryorganizer.api.RecipeDetails) {
        // Implementation for using recipe
        Toast.makeText(this, "Recipe ingredients deducted from grocery list", Toast.LENGTH_SHORT).show()
        finish()
    }
}
