package com.example.smartgroceryorganizer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgroceryorganizer.databinding.ItemGroceryBinding

class GroceryAdapter(
    private val onClick: (GroceryItem) -> Unit,
    private val onStarToggle: (GroceryItem) -> Unit = {}
) : ListAdapter<GroceryItem, GroceryAdapter.VH>(DiffCallback()) {

    private fun getExpiryWarningDays(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(
            "SmartGroceryOrganizerPrefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getInt("expiry_warning_days", 3)
    }

    inner class VH(val binding: ItemGroceryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroceryItem) {
            binding.tvName.text = item.name
            binding.tvCategory.text = item.category
            binding.tvQuantity.text = item.quantity
            binding.tvExpiry.text = binding.root.context.getString(R.string.expires_format, item.expiry)

            val expiryWarningDays = getExpiryWarningDays(binding.root.context)
            val isExpiringSoon = item.daysLeft <= expiryWarningDays

            binding.tvDays.text = binding.root.context.getString(R.string.days_left_format, item.daysLeft)

            when {
                item.urgent -> {
                    binding.tvDays.setBackgroundResource(R.drawable.bg_tag_urgent)
                    binding.tvDays.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red_700))
                    binding.categoryIndicator.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.red_600))
                }
                isExpiringSoon -> {
                    binding.tvDays.setBackgroundResource(R.drawable.bg_tag_warning)
                    binding.tvDays.setTextColor(ContextCompat.getColor(binding.root.context, R.color.orange_700))
                    binding.categoryIndicator.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.orange_700))
                }
                else -> {
                    binding.tvDays.setBackgroundResource(R.drawable.bg_tag_ok)
                    binding.tvDays.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green_700))
                    binding.categoryIndicator.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primary))
                }
            }

            if (isExpiringSoon && !item.urgent) {
                binding.tvExpiry.text = "${binding.root.context.getString(R.string.expires_format, item.expiry)}"
                binding.tvExpiry.setTextColor(ContextCompat.getColor(binding.root.context, R.color.orange_700))
            } else {
                binding.tvExpiry.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_secondary))
            }

            binding.btnStarToggle.setImageResource(
                if (item.urgent) android.R.drawable.star_big_on
                else android.R.drawable.star_big_off
            )

            binding.btnStarToggle.setOnClickListener {
                onStarToggle(item)
            }

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemGroceryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }


    class DiffCallback : DiffUtil.ItemCallback<GroceryItem>() {
        override fun areItemsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
            return oldItem == newItem
        }
    }
}