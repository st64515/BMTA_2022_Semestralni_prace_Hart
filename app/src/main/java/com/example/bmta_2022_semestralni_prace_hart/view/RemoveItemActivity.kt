package com.example.bmta_2022_semestralni_prace_hart.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityRemoveItemBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import model.WarehouseItemNotFoundException
import viewmodel.Warehouse


class RemoveItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemoveItemBinding
    private lateinit var warehouse : Warehouse
    private val data = Intent()
    private val barcodeLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->

            if (result.contents == null) {
                Toast.makeText(this@RemoveItemActivity, "Načítání zrušeno", Toast.LENGTH_LONG)
                    .show()
            } else {
                binding.editTextItemCode.setText(result.contents.toString())

                try {

                binding.textItemDescription.text =
                    warehouse.findWarehouseItem(result.contents).description
                }
                catch (ex :WarehouseItemNotFoundException)
                {
                    Toast.makeText(this@RemoveItemActivity, "Načtený kód není definován", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_item)

        warehouse= intent.getSerializableExtra("warehouse") as Warehouse

        binding = ActivityRemoveItemBinding.inflate(layoutInflater)
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
                binding.textItemDescription.text = warehouse.findWarehouseItem(binding.editTextItemCode.text.toString()).description
                }catch (ex:WarehouseItemNotFoundException)
                {
                    binding.textItemDescription.text = "Nenalezeno..."
                }
            }
        })


        binding.buttonDelete.setOnClickListener() {
            if (binding.editTextItemCode.text.isEmpty()) {
                Toast.makeText(this, "Zadejte kód položky.", Toast.LENGTH_LONG).show()
            } else {
                data.putExtra("codeToRemove", binding.editTextItemCode.text.toString())
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