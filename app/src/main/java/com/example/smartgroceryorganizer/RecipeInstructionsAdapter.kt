package com.example.smartgroceryorganizer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgroceryorganizer.api.InstructionStep

/**
 * Adapter for displaying recipe instructions
 */
class RecipeInstructionsAdapter : ListAdapter<InstructionStep, RecipeInstructionsAdapter.InstructionViewHolder>(
    InstructionDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instruction, parent, false)
        return InstructionViewHolder(view)
    }

    override fun onBindViewHolder(holder: InstructionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InstructionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stepNumber: TextView = itemView.findViewById(R.id.tvStepNumber)
        private val stepText: TextView = itemView.findViewById(R.id.tvStepText)
        private val stepDuration: TextView = itemView.findViewById(R.id.tvStepDuration)

        fun bind(step: InstructionStep) {
            stepNumber.text = "Step ${step.number}"
            stepText.text = step.step

            // Display duration if available
            step.length?.let { length ->
                stepDuration.text = "${length.number} ${length.unit}"
                stepDuration.visibility = View.VISIBLE
            } ?: run {
                stepDuration.visibility = View.GONE
            }
        }
    }

    class InstructionDiffCallback : DiffUtil.ItemCallback<InstructionStep>() {
        override fun areItemsTheSame(
            oldItem: InstructionStep,
            newItem: InstructionStep
        ): Boolean {
            return oldItem.number == newItem.number
        }

        override fun areContentsTheSame(
            oldItem: InstructionStep,
            newItem: InstructionStep
        ): Boolean {
            return oldItem == newItem
        }
    }
}

