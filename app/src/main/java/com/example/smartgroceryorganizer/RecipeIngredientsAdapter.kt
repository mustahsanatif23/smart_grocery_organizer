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
import com.example.smartgroceryorganizer.api.ExtendedIngredient

/**
 * Adapter for displaying recipe ingredients
 */
class RecipeIngredientsAdapter : ListAdapter<ExtendedIngredient, RecipeIngredientsAdapter.IngredientViewHolder>(
    IngredientDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientImage: ImageView = itemView.findViewById(R.id.ivIngredientImage)
        private val ingredientName: TextView = itemView.findViewById(R.id.tvIngredientName)
        private val ingredientAmount: TextView = itemView.findViewById(R.id.tvIngredientAmount)

        fun bind(ingredient: ExtendedIngredient) {
            ingredientName.text = ingredient.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }

            // Format amount and unit
            val amount = if (ingredient.amount % 1.0 == 0.0) {
                ingredient.amount.toInt().toString()
            } else {
                String.format("%.2f", ingredient.amount)
            }
            ingredientAmount.text = "$amount ${ingredient.unit}"

            // Load ingredient image if available
            ingredient.image?.let { imageName ->
                val imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/$imageName"
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_recipe_placeholder)
                    .error(R.drawable.ic_recipe_placeholder)
                    .centerCrop()
                    .into(ingredientImage)
            } ?: run {
                ingredientImage.setImageResource(R.drawable.ic_recipe_placeholder)
            }
        }
    }

    class IngredientDiffCallback : DiffUtil.ItemCallback<ExtendedIngredient>() {
        override fun areItemsTheSame(
            oldItem: ExtendedIngredient,
            newItem: ExtendedIngredient
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ExtendedIngredient,
            newItem: ExtendedIngredient
        ): Boolean {
            return oldItem == newItem
        }
    }
}

