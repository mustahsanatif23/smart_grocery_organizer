package com.example.smartgroceryorganizer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartgroceryorganizer.api.ApiResult
import com.example.smartgroceryorganizer.api.RecipeRepository
import com.example.smartgroceryorganizer.api.RetrofitClient
import com.example.smartgroceryorganizer.api.SpoonacularRecipe
import com.example.smartgroceryorganizer.databinding.ActivityRecipesBinding
import kotlinx.coroutines.launch

class RecipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecipesBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var groceryViewModel: GroceryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = "Recipe Suggestions"
        binding.toolbar.setNavigationOnClickListener { navigateToHome() }

        recipeRepository = RecipeRepository(RetrofitClient.apiService)
        groceryViewModel = ViewModelProvider(this)[GroceryViewModel::class.java]

        setupRecyclerView()

        binding.btnSearchRecipes.setOnClickListener {
            fetchRecipesFromGroceryList()
        }

        setupBottomNavigation()
        setupBackPressHandler()

        showEmptyState("Click the button above to search for recipes using your grocery items")
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_recipes
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_recipes
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
        binding.bottomNavigation.selectedItemId = R.id.nav_bottom_recipes

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bottom_home -> {
                    navigateToActivity(MainActivity::class.java, "left")
                    true
                }
                R.id.nav_bottom_recipes -> true
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
        finish()
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter { recipe ->
            onRecipeClicked(recipe)
        }

        binding.recyclerViewRecipes.apply {
            layoutManager = LinearLayoutManager(this@RecipesActivity)
            adapter = recipeAdapter
        }
    }

    private fun fetchRecipesFromGroceryList() {
        groceryViewModel.groceryItems.observe(this) { groceryItems ->
            if (groceryItems.isNullOrEmpty()) {
                showEmptyState("No grocery items found. Please add some items to your grocery list first.")
                return@observe
            }

            val ingredients = groceryItems
                .map { it.name.trim() }
                .filter { it.isNotBlank() }
                .joinToString(",")

            if (ingredients.isBlank()) {
                showEmptyState("No valid ingredients found in your grocery list")
                return@observe
            }

            searchRecipes(ingredients)
        }
    }

    private fun searchRecipes(ingredients: String) {
        showLoading()

        lifecycleScope.launch {
            when (val result = recipeRepository.findRecipesByIngredients(ingredients, 20)) {
                is ApiResult.Success -> {
                    val recipes = result.data
                    if (recipes.isEmpty()) {
                        showEmptyState("No recipes found with your ingredients. Try adding more items to your grocery list.")
                    } else {
                        showRecipes(recipes)

                        val bestMatch = recipes.firstOrNull()
                        val message = if (bestMatch != null && bestMatch.missedIngredientCount == 0) {
                            "Found ${recipes.size} recipes! Top matches need no extra ingredients!"
                        } else if (bestMatch != null && bestMatch.missedIngredientCount <= 2) {
                            "Found ${recipes.size} recipes! Showing recipes with fewest extra ingredients first."
                        } else {
                            "Found ${recipes.size} recipes! Sorted by minimal additional ingredients."
                        }

                        Toast.makeText(
                            this@RecipesActivity,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                is ApiResult.Error -> {
                    showEmptyState("Error: ${result.message}")
                    Toast.makeText(
                        this@RecipesActivity,
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

    private fun onRecipeClicked(recipe: SpoonacularRecipe) {
        // Open recipe details activity
        val intent = RecipeDetailsActivity.newIntent(this, recipe.id, recipe.title)
        startActivity(intent)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewRecipes.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
    }

    private fun showRecipes(recipes: List<SpoonacularRecipe>) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerViewRecipes.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        recipeAdapter.submitList(recipes)
    }

    private fun showEmptyState(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerViewRecipes.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.tvEmptyState.text = message
    }
}
