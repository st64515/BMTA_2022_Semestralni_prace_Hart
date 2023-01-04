package com.example.bmta_2022_semestralni_prace_hart.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityBorrowedItemsBinding
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityNewItemBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import model.WarehouseItem

class NewItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewItemBinding
    private val data = Intent()
    private val barcodeLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->

            if (result.contents == null) {
                Toast.makeText(this@NewItemActivity, "Načítání zrušeno", Toast.LENGTH_LONG).show()
            } else {
                binding.editTextItemCode.setText(result.contents.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_item)

        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.hide()
        this.window.statusBarColor =
            this.resources.getColor(com.example.bmta_2022_semestralni_prace_hart.R.color.blue_darker2)

        binding.buttonAdd.setOnClickListener() {
            if (binding.editTextItemCode.text.isEmpty()) {
                Toast.makeText(this, "Zadejte kód položky.", Toast.LENGTH_LONG).show()
            } else if (binding.editTextItemDescription.text.isEmpty()) {
                Toast.makeText(this, "Zadejte popis položky.", Toast.LENGTH_LONG).show()
            } else {
                data.putExtra(
                    "newItem", WarehouseItem(
                        binding.editTextItemCode.text.toString(),
                        binding.editTextItemDescription.text.toString()
                    )
                )
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