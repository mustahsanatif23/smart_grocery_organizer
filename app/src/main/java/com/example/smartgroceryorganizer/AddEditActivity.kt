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
    private var existingUrgentStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editItemId = intent.getIntExtra("item_id", 0)
        isEditMode = editItemId != 0

        binding.toolbar.title = if (isEditMode) "Edit Item" else "Add New Item"
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupCategorySpinner()

        if (isEditMode) {
            loadItemData()
        }

        setupDatePicker()

        binding.btnSave.setOnClickListener {
            saveItem()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupCategorySpinner() {
        val categories = resources.getStringArray(R.array.grocery_categories)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etExpiry.setOnClickListener {
            val calendar = Calendar.getInstance()

            val currentDate = binding.etExpiry.text.toString()
            if (currentDate.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = sdf.parse(currentDate)
                    if (date != null) {
                        calendar.time = date
                    }
                } catch (e: Exception) {
                    // Use current date if parsing fails
                }
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = String.format(
                        Locale.getDefault(),
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                    )
                    binding.etExpiry.setText(selectedDate)
                },
                year,
                month,
                day
            )

            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }
    }

    private fun loadItemData() {
        binding.etName.setText(intent.getStringExtra("item_name") ?: "")
        binding.etQuantity.setText(intent.getStringExtra("item_quantity") ?: "")
        binding.etExpiry.setText(intent.getStringExtra("item_expiry") ?: "")
        existingUrgentStatus = intent.getBooleanExtra("item_urgent", false)

        val category = intent.getStringExtra("item_category") ?: ""
        val categories = resources.getStringArray(R.array.grocery_categories)
        val categoryIndex = categories.indexOf(category)
        if (categoryIndex >= 0) {
            binding.spinnerCategory.setSelection(categoryIndex)
        }
    }

    private fun saveItem() {
        val name = binding.etName.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val quantity = binding.etQuantity.text.toString().trim()
        val expiry = binding.etExpiry.text.toString().trim()

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

        val daysLeft = calculateDaysLeft(expiry)
        val urgent = if (isEditMode) existingUrgentStatus else false

        if (isEditMode) {
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

        finish()
    }

    /** Calculate days remaining until expiry, normalized to midnight for accuracy */
    private fun calculateDaysLeft(expiryDate: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = sdf.parse(expiryDate)

            if (parsedDate != null) {
                val todayCal = Calendar.getInstance()
                todayCal.set(Calendar.HOUR_OF_DAY, 0)
                todayCal.set(Calendar.MINUTE, 0)
                todayCal.set(Calendar.SECOND, 0)
                todayCal.set(Calendar.MILLISECOND, 0)

                val expiryCal = Calendar.getInstance()
                expiryCal.time = parsedDate
                expiryCal.set(Calendar.HOUR_OF_DAY, 0)
                expiryCal.set(Calendar.MINUTE, 0)
                expiryCal.set(Calendar.SECOND, 0)
                expiryCal.set(Calendar.MILLISECOND, 0)

                val diff = expiryCal.timeInMillis - todayCal.timeInMillis
                TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
}
