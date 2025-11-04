package com.example.smartgroceryorganizer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smartgroceryorganizer.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: GroceryViewModel by viewModels()
    private var itemId: Int = 0
    private var itemUrgent: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        itemId = intent.getIntExtra("item_id", 0)
        val itemName = intent.getStringExtra("item_name") ?: "-"
        val itemCategory = intent.getStringExtra("item_category") ?: "-"
        val itemQuantity = intent.getStringExtra("item_quantity") ?: "-"
        val itemExpiry = intent.getStringExtra("item_expiry") ?: "-"
        val itemDays = intent.getIntExtra("item_days", 0)
        itemUrgent = intent.getBooleanExtra("item_urgent", false)

        binding.tvName.text = itemName
        binding.tvCategory.text = itemCategory
        binding.tvQuantity.text = itemQuantity
        binding.tvExpiry.text = itemExpiry
        binding.tvDays.text = "$itemDays days"
        binding.tvDays.setTextColor(
            if (itemUrgent) resources.getColor(R.color.red_600, null)
            else resources.getColor(R.color.green_600, null)
        )

        binding.btnEdit.setOnClickListener {
            editItem()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun editItem() {
        val intent = Intent(this, AddEditActivity::class.java).apply {
            putExtra("item_id", itemId)
            putExtra("item_name", binding.tvName.text.toString())
            putExtra("item_category", binding.tvCategory.text.toString())
            putExtra("item_quantity", binding.tvQuantity.text.toString())
            putExtra("item_expiry", binding.tvExpiry.text.toString())
            putExtra("item_urgent", itemUrgent)
        }
        startActivity(intent)
        finish()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete") { _, _ ->
                deleteItem()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItem() {
        viewModel.removeItemById(itemId)
        Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
