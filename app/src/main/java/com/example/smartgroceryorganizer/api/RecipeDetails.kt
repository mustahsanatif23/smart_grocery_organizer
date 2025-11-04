package com.example.smartgroceryorganizer.api

import com.google.gson.annotations.SerializedName

/**
 * Data class representing full recipe details from Spoonacular API
 */
data class RecipeDetails(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("image")
    val image: String?,

    @SerializedName("imageType")
    val imageType: String?,

    @SerializedName("servings")
    val servings: Int,

    @SerializedName("readyInMinutes")
    val readyInMinutes: Int,

    @SerializedName("cookingMinutes")
    val cookingMinutes: Int?,

    @SerializedName("preparationMinutes")
    val preparationMinutes: Int?,

    @SerializedName("sourceUrl")
    val sourceUrl: String?,

    @SerializedName("spoonacularSourceUrl")
    val spoonacularSourceUrl: String?,

    @SerializedName("aggregateLikes")
    val aggregateLikes: Int?,

    @SerializedName("healthScore")
    val healthScore: Double?,

    @SerializedName("pricePerServing")
    val pricePerServing: Double?,

    @SerializedName("cheap")
    val cheap: Boolean?,

    @SerializedName("creditsText")
    val creditsText: String?,

    @SerializedName("sourceName")
    val sourceName: String?,

    @SerializedName("extendedIngredients")
    val extendedIngredients: List<ExtendedIngredient>,

    @SerializedName("summary")
    val summary: String?,

    @SerializedName("cuisines")
    val cuisines: List<String>?,

    @SerializedName("dishTypes")
    val dishTypes: List<String>?,

    @SerializedName("diets")
    val diets: List<String>?,

    @SerializedName("occasions")
    val occasions: List<String>?,

    @SerializedName("instructions")
    val instructions: String?,

    @SerializedName("analyzedInstructions")
    val analyzedInstructions: List<AnalyzedInstruction>?,

    @SerializedName("sustainable")
    val sustainable: Boolean?,

    @SerializedName("vegetarian")
    val vegetarian: Boolean?,

    @SerializedName("vegan")
    val vegan: Boolean?,

    @SerializedName("glutenFree")
    val glutenFree: Boolean?,

    @SerializedName("dairyFree")
    val dairyFree: Boolean?,

    @SerializedName("veryHealthy")
    val veryHealthy: Boolean?,

    @SerializedName("veryPopular")
    val veryPopular: Boolean?
)

/**
 * Extended ingredient details
 */
data class ExtendedIngredient(
    @SerializedName("id")
    val id: Int,

    @SerializedName("aisle")
    val aisle: String?,

    @SerializedName("image")
    val image: String?,

    @SerializedName("name")
    val name: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("unitShort")
    val unitShort: String?,

    @SerializedName("unitLong")
    val unitLong: String?,

    @SerializedName("original")
    val original: String,

    @SerializedName("originalName")
    val originalName: String?,

    @SerializedName("meta")
    val meta: List<String>?,

    @SerializedName("measures")
    val measures: Measures?
)

/**
 * Measurement details
 */
data class Measures(
    @SerializedName("us")
    val us: Measurement?,

    @SerializedName("metric")
    val metric: Measurement?
)

data class Measurement(
    @SerializedName("amount")
    val amount: Double,

    @SerializedName("unitShort")
    val unitShort: String?,

    @SerializedName("unitLong")
    val unitLong: String?
)

/**
 * Analyzed instructions with steps
 */
data class AnalyzedInstruction(
    @SerializedName("name")
    val name: String?,

    @SerializedName("steps")
    val steps: List<InstructionStep>
)

/**
 * Individual instruction step
 */
data class InstructionStep(
    @SerializedName("number")
    val number: Int,

    @SerializedName("step")
    val step: String,

    @SerializedName("ingredients")
    val ingredients: List<StepIngredient>?,

    @SerializedName("equipment")
    val equipment: List<StepEquipment>?,

    @SerializedName("length")
    val length: StepLength?
)

/**
 * Ingredient used in a step
 */
data class StepIngredient(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("image")
    val image: String?
)

/**
 * Equipment used in a step
 */
data class StepEquipment(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("image")
    val image: String?
)

/**
 * Duration of a step
 */
data class StepLength(
    @SerializedName("number")
    val number: Int,

    @SerializedName("unit")
    val unit: String
)

