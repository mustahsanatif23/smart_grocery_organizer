package com.example.smartgroceryorganizer

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.smartgroceryorganizer.databinding.ActivityAddEditBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditBinding
    private val viewModel: GroceryViewModel by viewModels()
    private var editItemId: Int = 0
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if we're in edit mode
        editItemId = intent.getIntExtra("item_id", 0)
        isEditMode = editItemId != 0

        // Update toolbar title based on mode
        binding.toolbar.title = if (isEditMode) "Edit Item" else "Add New Item"
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Setup category spinner
        setupCategorySpinner()

        // Load existing item data if in edit mode
        if (isEditMode) {
            loadItemData()
        }

        // Setup DatePicker for expiry date
        setupDatePicker()

        binding.btnSave.setOnClickListener {
            saveItem()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupCategorySpinner() {
        // Get the categories from resources
        val categories = resources.getStringArray(R.array.grocery_categories)

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etExpiry.setOnClickListener {
            val calendar = Calendar.getInstance()

            // If there's already a date in the field, parse it and set as default
            val currentDate = binding.etExpiry.text.toString()
            if (currentDate.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = sdf.parse(currentDate)
                    if (date != null) {
                        calendar.time = date
                    }
                } catch (e: Exception) {
                    // If parsing fails, use current date
                }
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format the selected date as YYYY-MM-DD
                    val selectedDate = String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1, // Month is 0-based
                        selectedDay
                    )
                    binding.etExpiry.setText(selectedDate)
                },
                year,
                month,
                day
            )

            // Set minimum date to today to prevent selecting past dates
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

            datePickerDialog.show()
        }
    }

    private fun loadItemData() {
        // Populate fields with existing data
        binding.etName.setText(intent.getStringExtra("item_name") ?: "")
        binding.etQuantity.setText(intent.getStringExtra("item_quantity") ?: "")
        binding.etExpiry.setText(intent.getStringExtra("item_expiry") ?: "")

        // Set category spinner selection
        val category = intent.getStringExtra("item_category") ?: ""
        val categories = resources.getStringArray(R.array.grocery_categories)
        val categoryIndex = categories.indexOf(category)
        if (categoryIndex >= 0) {
            binding.spinnerCategory.setSelection(categoryIndex)
        }
    }

    private fun saveItem() {
        // Get input values
        val name = binding.etName.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val quantity = binding.etQuantity.text.toString().trim()
        val expiry = binding.etExpiry.text.toString().trim()

        // Validate inputs
        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            binding.etName.requestFocus()
            return
        }

        if (quantity.isEmpty()) {
            binding.etQuantity.error = "Quantity is required"
            binding.etQuantity.requestFocus()
            return
        }

        if (expiry.isEmpty()) {
            binding.etExpiry.error = "Expiry date is required"
            binding.etExpiry.requestFocus()
            return
        }

        // Calculate days left and urgent status
        val daysLeft = calculateDaysLeft(expiry)
        val urgent = daysLeft <= 3 && daysLeft >= 0

        if (isEditMode) {
            // Update existing item
            val updatedItem = GroceryItem(
                id = editItemId,
                name = name,
                category = category,
                quantity = quantity,
                expiry = expiry,
                daysLeft = daysLeft,
                urgent = urgent
            )
            viewModel.updateItem(updatedItem)
            Toast.makeText(this, "Item updated successfully!", Toast.LENGTH_SHORT).show()
        } else {
            // Create new grocery item
            val newItem = GroceryItem(
                name = name,
                category = category,
                quantity = quantity,
                expiry = expiry,
                daysLeft = daysLeft,
                urgent = urgent
            )
            viewModel.addItem(newItem)
            Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show()
        }

        // Close activity and return to MainActivity
        finish()
    }

    private fun calculateDaysLeft(expiryDate: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = sdf.parse(expiryDate)
            val today = Calendar.getInstance().time

            if (expiry != null) {
                val diff = expiry.time - today.time
                TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
}
