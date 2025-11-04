package com.example.smartgroceryorganizer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgroceryorganizer.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onClick: (CategoryData) -> Unit
) : ListAdapter<CategoryData, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, onClick)
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryData, onClick: (CategoryData) -> Unit) {
            binding.tvCategoryName.text = category.name
            binding.tvItemCount.text = "${category.itemCount} items"
            binding.ivCategoryIcon.setImageResource(category.icon)

            if (category.expiringSoonCount > 0) {
                binding.tvExpiringSoon.visibility = android.view.View.VISIBLE
                binding.tvExpiringSoon.text = "${category.expiringSoonCount} expiring"
            } else {
                binding.tvExpiringSoon.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener {
                onClick(category)
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryData>() {
        override fun areItemsTheSame(oldItem: CategoryData, newItem: CategoryData): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: CategoryData, newItem: CategoryData): Boolean {
            return oldItem == newItem
        }
    }
}

