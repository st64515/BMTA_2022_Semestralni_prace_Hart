package com.example.bmta_2022_semestralni_prace_hart.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityMainBinding
import com.example.bmta_2022_semestralni_prace_hart.viewmodel.WarehouseFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import model.WarehouseItem
import model.WarehouseItemNotFoundException
import org.json.JSONException
import viewmodel.Warehouse
import java.io.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var warehouse: Warehouse
    private lateinit var caller: String
    private val mapper = jacksonObjectMapper()

    private val barcodeLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->

            if (result.contents == null) {
                Toast.makeText(this@MainActivity, "Načítání zrušeno", Toast.LENGTH_LONG).show()
            } else {
                handleReadCode(result.contents)
                writeToFile(makeJsonString(),applicationContext) //Uložení do souboru
            }
        }
    private val defineItemIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                var newItem = result.data?.getSerializableExtra("newItem") as WarehouseItem
                if (newItem != null) {
                    try {
                        warehouse.defineNewWarehouseItem(newItem)
                        writeToFile(makeJsonString(),applicationContext) //Uložení do souboru
                    } catch (ex: java.lang.IllegalArgumentException) {
                        Toast.makeText(
                            this@MainActivity,
                            "Chyba: Položka s tímto kódem již existuje!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.hide()
        this.window.statusBarColor =
            this.resources.getColor(com.example.bmta_2022_semestralni_prace_hart.R.color.blue_darker2)

        val savedItemsJson = readFromFile(applicationContext)

        try {
            var factory = WarehouseFactory(savedItemsJson)
            warehouse = ViewModelProvider(this, factory).get(Warehouse::class.java)
        } catch (ex: JSONException) {
            Toast.makeText(
                this@MainActivity,
                ex.message,
                Toast.LENGTH_LONG
            ).show()
            this.finishAffinity()
        }

        binding.buttonScan.setOnClickListener() {buttonScanOnClick()  }
        binding.settingsButton.setOnClickListener() { onMenuClicked() }
    }

    private fun buttonScanOnClick() {

        scanCode("button")

        writeToFile(makeJsonString(),applicationContext)
    }


    private fun onMenuClicked() {
        // Initializing the popup menu and giving the reference as current context
        val popupMenu = PopupMenu(this@MainActivity, binding.settingsButton)

        // Inflating popup menu from popup_menu.xml file
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu())
        popupMenu.setOnMenuItemClickListener { menuItem -> //Vyvolej obsluhu, podle druhu menuItemu
            if (menuItem.titleCondensed == "showAllItems") {
                val i = Intent(this, AllItemsActivity::class.java)
                i.putExtra("warehouse", warehouse)
                startActivity(i)
            } else if (menuItem.titleCondensed == "showBorrowedItems") {
                val i = Intent(this, BorrowedItemsActivity::class.java)
                i.putExtra("warehouse", warehouse)
                startActivity(i)
            } else if (menuItem.titleCondensed == "defineItem") {
                defineItemIntentLauncher.launch(Intent(this, NewItemActivity::class.java))
            } else {
                scanCode(menuItem.titleCondensed.toString())
            }
            true
        }
        // Showing the popup menu
        popupMenu.show()
    }

    private fun scanCode(caller: String) {
        val options = ScanOptions()
        options.setPrompt("Stiskni tlačítko volume nahoru pro rozsvícení baterky")
        options.setBeepEnabled(false)
        options.setOrientationLocked(true)
        options.captureActivity = CaptureAct().javaClass
        this.caller = caller
        barcodeLauncher.launch(options)
    }

    private fun handleReadCode(contents: String) {
        var item: WarehouseItem
        when (caller) {
            "button" -> {
                try {
                    item = warehouse.findWarehouseItem(contents)
                } catch (e: WarehouseItemNotFoundException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Neznámá položka: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                if (binding.radioButtonBorrow.isChecked) {
                    try {
                        warehouse.borrowWarehouseItem(item.id)

                        Toast.makeText(
                            this@MainActivity,
                            "Přesouvám do vypůjčených položku: " + item.description,
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: WarehouseItemNotFoundException) {
                        Toast.makeText(
                            this@MainActivity,
                            "Tato položka je již vypůjčená: " + item.description,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else if (binding.radioButtonReturn.isChecked) {
                    try {
                        warehouse.returnWarehouseItem(item.id)

                        Toast.makeText(
                            this@MainActivity,
                            "Vracím položku: " + item.description,
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: WarehouseItemNotFoundException) {
                        Toast.makeText(
                            this@MainActivity,
                            "Tato položka není vypůjčená: " + item.description,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }
            "removeItem" -> {
                try {
                    item = warehouse.findWarehouseItem(contents)
                } catch (e: WarehouseItemNotFoundException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Neznámá položka: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                try {
                    warehouse.removeWarehouseItem(contents)
                    Toast.makeText(
                        this@MainActivity,
                        "Ze skladu smazána položka: " + item.description,
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: WarehouseItemNotFoundException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Tato položka nebyla definována: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    //TODO: dodělat ukládání po každém stisknutí tlačítka
/*
    fun readJsonStringFromFile(): String {
        var string: String? = ""
        val stringBuilder = StringBuilder()
        val inputStream: InputStream = this.resources.openRawResource(R.raw.saved_items)
        val reader = BufferedReader(InputStreamReader(inputStream))
        while (true) {
            try {
                if (reader.readLine().also { string = it } == null) break
            } catch (e: IOException) {
                e.printStackTrace()
            }
            stringBuilder.append(string).append("\n")
        }
        inputStream.close()
        return stringBuilder.toString()
    }*/

    private fun makeJsonString():String{
        val stringBuilder = StringBuilder()

        stringBuilder.append("{")

        stringBuilder.append("\"allItems\": ")
        stringBuilder.append(mapper.writeValueAsString(warehouse.allItemsList()))
        stringBuilder.append(", ")

        stringBuilder.append("\"borrowedCodes\": ")
        stringBuilder.append(mapper.writeValueAsString(warehouse.borrowedCodesList()))
        stringBuilder.append("}")

        return stringBuilder.toString()
    }

    private fun writeToFile(data: String, context: Context) {
        try {
            val outputStreamWriter =
                OutputStreamWriter(context.openFileOutput("config.txt", MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }

    private fun readFromFile(context: Context): String {
        var ret = ""
        try {
            val inputStream: InputStream = context.openFileInput("config.txt")
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String? = ""
                val stringBuilder = java.lang.StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append("\n").append(receiveString)
                }
                inputStream.close()
                ret = stringBuilder.toString()
            }
        } catch (e: FileNotFoundException) {
            Log.e("login activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("login activity", "Can not read file: $e")
        }
        return ret
    }

}
