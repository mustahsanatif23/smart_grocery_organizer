package com.example.smartgroceryorganizer.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Handles Spoonacular API requests with error handling and result sorting */
class RecipeRepository(private val apiService: SpoonacularApiService) {

    companion object {
        private const val TAG = "RecipeRepository"
        const val API_KEY = "259e9ca7340f4c77b45b888f27aa7faa"
    }

    /**
     * Find recipes by ingredients, prioritizing those with minimal additional ingredients
     * @param ingredients Comma-separated ingredient list
     * @param numberOfRecipes Desired number of results
     * @return Sorted list prioritizing recipes with fewest missed ingredients
     */
    suspend fun findRecipesByIngredients(
        ingredients: String,
        numberOfRecipes: Int = 10
    ): ApiResult<List<SpoonacularRecipe>> {
        return withContext(Dispatchers.IO) {
            try {
                if (ingredients.isBlank()) {
                    return@withContext ApiResult.Error("Please provide at least one ingredient")
                }

                Log.d(TAG, "Fetching recipes for ingredients: $ingredients")

                val fetchCount = (numberOfRecipes * 1.5).toInt().coerceAtMost(50)

                val response = apiService.findRecipesByIngredients(
                    ingredients = ingredients,
                    number = fetchCount,
                    ranking = 2,
                    ignorePantry = true,
                    apiKey = API_KEY
                )

                if (response.isSuccessful) {
                    val recipes = response.body()
                    if (recipes != null) {
                        Log.d(TAG, "Successfully fetched ${recipes.size} recipes")

                        /** Sort by: 1) Fewest missed ingredients 2) Most used ingredients 3) Highest likes */
                        val sortedRecipes = recipes.sortedWith(
                            compareBy<SpoonacularRecipe> { it.missedIngredientCount }
                                .thenByDescending { it.usedIngredientCount }
                                .thenByDescending { it.likes ?: 0 }
                        ).take(numberOfRecipes)

                        Log.d(TAG, "Sorted and limited to ${sortedRecipes.size} recipes (prioritizing minimal additional ingredients)")
                        return@withContext ApiResult.Success(sortedRecipes)
                    } else {
                        Log.e(TAG, "Response body is null")
                        return@withContext ApiResult.Error("No recipes found")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Invalid API key"
                        402 -> "API quota exceeded"
                        404 -> "No recipes found"
                        429 -> "Too many requests. Please try again later"
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    Log.e(TAG, "API Error: $errorMsg")
                    return@withContext ApiResult.Error(errorMsg, response.code())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching recipes", e)
                val errorMsg = when {
                    e is java.net.UnknownHostException -> "No internet connection"
                    e is java.net.SocketTimeoutException -> "Connection timeout. Please try again"
                    else -> "Error: ${e.localizedMessage ?: "Unknown error occurred"}"
                }
                return@withContext ApiResult.Error(errorMsg)
            }
        }
    }

    /**
     * Get complete recipe details including ingredients and instructions
     * @param recipeId Recipe identifier
     * @param includeNutrition Whether to include nutritional information
     */
    suspend fun getRecipeDetails(
        recipeId: Int,
        includeNutrition: Boolean = false
    ): ApiResult<RecipeDetails> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching recipe details for ID: $recipeId")

                val response = apiService.getRecipeInformation(
                    id = recipeId,
                    includeNutrition = includeNutrition,
                    apiKey = API_KEY
                )

                if (response.isSuccessful) {
                    val recipeDetails = response.body()
                    if (recipeDetails != null) {
                        Log.d(TAG, "Successfully fetched recipe details: ${recipeDetails.title}")
                        return@withContext ApiResult.Success(recipeDetails)
                    } else {
                        Log.e(TAG, "Response body is null")
                        return@withContext ApiResult.Error("Recipe details not found")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Invalid API key"
                        402 -> "API quota exceeded"
                        404 -> "Recipe not found"
                        429 -> "Too many requests. Please try again later"
                        else -> "Error: ${response.code()} - ${response.message()}"
                    }
                    Log.e(TAG, "API Error: $errorMsg")
                    return@withContext ApiResult.Error(errorMsg, response.code())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching recipe details", e)
                val errorMsg = when {
                    e is java.net.UnknownHostException -> "No internet connection"
                    e is java.net.SocketTimeoutException -> "Connection timeout. Please try again"
                    else -> "Error: ${e.localizedMessage ?: "Unknown error occurred"}"
                }
                return@withContext ApiResult.Error(errorMsg)
            }
        }
    }
}

