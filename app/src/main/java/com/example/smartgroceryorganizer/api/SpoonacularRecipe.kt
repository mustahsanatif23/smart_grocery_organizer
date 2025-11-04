package com.example.smartgroceryorganizer.api

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a recipe from Spoonacular API's findByIngredients endpoint
 */
data class SpoonacularRecipe(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("image")
    val image: String,

    @SerializedName("imageType")
    val imageType: String?,

    @SerializedName("usedIngredientCount")
    val usedIngredientCount: Int,

    @SerializedName("missedIngredientCount")
    val missedIngredientCount: Int,

    @SerializedName("missedIngredients")
    val missedIngredients: List<Ingredient>,

    @SerializedName("usedIngredients")
    val usedIngredients: List<Ingredient>,

    @SerializedName("unusedIngredients")
    val unusedIngredients: List<Ingredient>?,

    @SerializedName("likes")
    val likes: Int?
)

data class Ingredient(
    @SerializedName("id")
    val id: Int,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("unitLong")
    val unitLong: String?,

    @SerializedName("unitShort")
    val unitShort: String?,

    @SerializedName("aisle")
    val aisle: String?,

    @SerializedName("name")
    val name: String,

    @SerializedName("original")
    val original: String,

    @SerializedName("originalName")
    val originalName: String?,

    @SerializedName("meta")
    val meta: List<String>?,

    @SerializedName("image")
    val image: String?
)

