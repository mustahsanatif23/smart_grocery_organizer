package com.example.smartgroceryorganizer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgroceryorganizer.databinding.ItemGroceryBinding

class GroceryAdapter(
    private val onClick: (GroceryItem) -> Unit,
    private val onStarToggle: (GroceryItem) -> Unit = {},
    private val onLongPress: () -> Unit = {}
) : ListAdapter<GroceryItem, GroceryAdapter.VH>(DiffCallback()) {

    private var isMultiSelectMode = false
    private val selectedItems = mutableSetOf<Int>()

    inner class VH(val binding: ItemGroceryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroceryItem) {
            binding.tvName.text = item.name
            binding.tvCategory.text = item.category
            binding.tvQuantity.text = item.quantity
            binding.tvExpiry.text = binding.root.context.getString(R.string.expires_format, item.expiry)

            // Check if item is expiring soon (2 days or fewer)
            val isExpiringSoon = item.daysLeft <= 2

            // Set days left text and color based on urgency and expiring soon status
            binding.tvDays.text = binding.root.context.getString(R.string.days_left_format, item.daysLeft)

            when {
                item.urgent -> {
                    // Urgent items - highest priority (red)
                    binding.tvDays.setBackgroundResource(R.drawable.bg_tag_urgent)
                    binding.tvDays.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red_700))
                    binding.categoryIndicator.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.red_600))
                }
                isExpiringSoon -> {
                    // Expiring soon items (2 days or fewer) - warning (orange/yellow)
                    binding.tvDays.setBackgroundResource(R.drawable.bg_tag_warning)
                    binding.tvDays.setTextColor(ContextCompat.getColor(binding.root.context, R.color.orange_700))
                    binding.categoryIndicator.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.orange_700))
                }
                else -> {
                    // Normal items - safe (green)
                    binding.tvDays.setBackgroundResource(R.drawable.bg_tag_ok)
                    binding.tvDays.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green_700))
                    binding.categoryIndicator.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primary))
                }
            }

            // Add "EXPIRING SOON!" indicator for items with 2 days or fewer
            if (isExpiringSoon && !item.urgent) {
                binding.tvExpiry.text = "${binding.root.context.getString(R.string.expires_format, item.expiry)}"
                binding.tvExpiry.setTextColor(ContextCompat.getColor(binding.root.context, R.color.orange_700))
            } else {
                binding.tvExpiry.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_secondary))
            }

            // Update star button based on urgency
            binding.btnStarToggle.setImageResource(
                if (item.urgent) android.R.drawable.star_big_on
                else android.R.drawable.star_big_off
            )

            // Star toggle click listener
            binding.btnStarToggle.setOnClickListener {
                onStarToggle(item)
            }

            // Multi-selection checkbox
            binding.cbSelect.visibility = if (isMultiSelectMode) View.VISIBLE else View.GONE
            binding.cbSelect.isChecked = selectedItems.contains(item.id)
            binding.cbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(item.id)
                } else {
                    selectedItems.remove(item.id)
                }
            }

            // Item click - toggle selection in multi-select mode, otherwise open details
            binding.root.setOnClickListener {
                if (isMultiSelectMode) {
                    binding.cbSelect.isChecked = !binding.cbSelect.isChecked
                } else {
                    onClick(item)
                }
            }

            // Long press to enter multi-select mode
            binding.root.setOnLongClickListener {
                if (!isMultiSelectMode) {
                    enterMultiSelectMode()
                    binding.cbSelect.isChecked = true
                }
                onLongPress()
                true
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

    fun enterMultiSelectMode() {
        isMultiSelectMode = true
        notifyDataSetChanged()
    }

    fun exitMultiSelectMode() {
        isMultiSelectMode = false
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<GroceryItem> {
        return currentList.filter { selectedItems.contains(it.id) }
    }

    fun isInMultiSelectMode() = isMultiSelectMode

    fun hasSelectedItems() = selectedItems.isNotEmpty()

    class DiffCallback : DiffUtil.ItemCallback<GroceryItem>() {
        override fun areItemsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GroceryItem, newItem: GroceryItem): Boolean {
            return oldItem == newItem
        }
    }
}