package com.example.smartgroceryorganizer.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit API interface for Spoonacular recipe service */
interface SpoonacularApiService {

    /**
     * Find recipes by available ingredients
     * @param ingredients Comma-separated ingredient list (e.g., "tomatoes,cheese,eggs")
     * @param number Number of results to return
     * @param ranking 1 = maximize used ingredients, 2 = minimize missing ingredients
     * @param ignorePantry Exclude common pantry items
     */
    @GET("recipes/findByIngredients")
    suspend fun findRecipesByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 10,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = true,
        @Query("apiKey") apiKey: String
    ): Response<List<SpoonacularRecipe>>

    /**
     * Get complete recipe information including ingredients and instructions
     * @param id Recipe identifier
     * @param includeNutrition Include nutritional data in response
     */
    @GET("recipes/{id}/information")
    suspend fun getRecipeInformation(
        @Path("id") id: Int,
        @Query("includeNutrition") includeNutrition: Boolean = false,
        @Query("apiKey") apiKey: String
    ): Response<RecipeDetails>
}

