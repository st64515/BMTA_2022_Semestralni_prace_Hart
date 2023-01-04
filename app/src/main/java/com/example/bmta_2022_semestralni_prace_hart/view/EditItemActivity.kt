package com.example.bmta_2022_semestralni_prace_hart.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityEditItemBinding
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityRemoveItemBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import model.WarehouseItem
import model.WarehouseItemNotFoundException
import viewmodel.Warehouse

class EditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditItemBinding
    private lateinit var warehouse: Warehouse
    private val data = Intent()
    private val barcodeLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->

            if (result.contents == null) {
                Toast.makeText(this@EditItemActivity, "Načítání zrušeno", Toast.LENGTH_LONG)
                    .show()
            } else {
                binding.editTextItemCode.setText(result.contents.toString())

                try {
                    var actualItem = warehouse.findWarehouseItem(result.contents)

                    binding.editTextItemDescription.setText(actualItem.description)
                    binding.editTextBorrowings.setText(actualItem.numberOfBorrowings.toString())

                } catch (ex: WarehouseItemNotFoundException) {
                    Toast.makeText(
                        this@EditItemActivity,
                        "Načtený kód není definován",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    binding.editTextItemDescription.setText("Nenalezeno...")
                    binding.editTextBorrowings.setText("Nenalezeno...")
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        warehouse= intent.getSerializableExtra("warehouse") as Warehouse

        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.hide()
        this.window.statusBarColor =
            this.resources.getColor(R.color.blue_darker2, null)

        binding.editTextItemCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                try {
                    var actualItem =
                        warehouse.findWarehouseItem(binding.editTextItemCode.text.toString())

                    binding.editTextItemDescription.setText(actualItem.description)
                    binding.editTextBorrowings.setText(actualItem.numberOfBorrowings.toString())
                } catch (ex: WarehouseItemNotFoundException) {
                    binding.editTextItemDescription.setText("Nenalezeno...")
                    binding.editTextBorrowings.setText("Nenalezeno...")
                }
            }
        })

        binding.buttonOK.setOnClickListener() {
            if (binding.editTextItemCode.text.isEmpty()) {
                Toast.makeText(this, "Zadejte kód položky.", Toast.LENGTH_LONG).show()
            } else if (binding.editTextItemDescription.text.isEmpty()) {
                Toast.makeText(this, "Zadejte popis položky.", Toast.LENGTH_LONG).show()
            } else if (binding.editTextBorrowings.text.isEmpty()) {
                Toast.makeText(this, "Zadejte počet výpůjček.", Toast.LENGTH_LONG).show()
            } else {
                data.putExtra("editedItem", WarehouseItem(
                    id= binding.editTextItemCode.text.toString(),
                    description = binding.editTextItemDescription.text.toString(),
                    numberOfBorrowings = binding.editTextBorrowings.text.toString().toInt()))
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }

        binding.buttonScanCode.setOnClickListener() {
            val options = ScanOptions()
            options.setPrompt("Stiskni tlačítko volume nahoru pro rozsvícení baterky")
            options.setBeepEnabled(false)
            options.setOrientationLocked(true)
            options.captureActivity = CaptureAct().javaClass
            barcodeLauncher.launch(options)
        }

    }
}