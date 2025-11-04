package com.example.smartgroceryorganizer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.smartgroceryorganizer.api.SpoonacularRecipe

/** Displays recipe suggestions from Spoonacular API */
class RecipeAdapter(
    private val onRecipeClick: (SpoonacularRecipe) -> Unit
) : ListAdapter<SpoonacularRecipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view, onRecipeClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecipeViewHolder(
        itemView: View,
        private val onRecipeClick: (SpoonacularRecipe) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val recipeImage: ImageView = itemView.findViewById(R.id.ivRecipeImage)
        private val recipeTitle: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        private val usedIngredientsText: TextView = itemView.findViewById(R.id.tvUsedIngredients)
        private val missedIngredientsText: TextView = itemView.findViewById(R.id.tvMissedIngredients)

        fun bind(recipe: SpoonacularRecipe) {
            recipeTitle.text = recipe.title
            usedIngredientsText.text = "✓ ${recipe.usedIngredientCount} ingredients you have"

            when (recipe.missedIngredientCount) {
                0 -> {
                    missedIngredientsText.text = "🌟 Perfect match! No extra ingredients needed"
                    missedIngredientsText.setTextColor(itemView.context.getColor(R.color.green_700))
                    missedIngredientsText.setTypeface(null, android.graphics.Typeface.BOLD)
                }
                1 -> {
                    missedIngredientsText.text = "⊕ Only 1 additional ingredient needed"
                    missedIngredientsText.setTextColor(itemView.context.getColor(R.color.green_600))
                    missedIngredientsText.setTypeface(null, android.graphics.Typeface.BOLD)
                }
                2 -> {
                    missedIngredientsText.text = "⊕ Just 2 additional ingredients needed"
                    missedIngredientsText.setTextColor(itemView.context.getColor(R.color.orange_500))
                    missedIngredientsText.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
                else -> {
                    missedIngredientsText.text = "⊕ ${recipe.missedIngredientCount} additional ingredients needed"
                    missedIngredientsText.setTextColor(itemView.context.getColor(R.color.orange_700))
                    missedIngredientsText.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
            }

            Glide.with(itemView.context)
                .load(recipe.image)
                .placeholder(R.drawable.ic_recipe_placeholder)
                .error(R.drawable.ic_recipe_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(recipeImage)

            itemView.setOnClickListener {
                onRecipeClick(recipe)
            }
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<SpoonacularRecipe>() {
        override fun areItemsTheSame(
            oldItem: SpoonacularRecipe,
            newItem: SpoonacularRecipe
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SpoonacularRecipe,
            newItem: SpoonacularRecipe
        ): Boolean {
            return oldItem == newItem
        }
    }
}

