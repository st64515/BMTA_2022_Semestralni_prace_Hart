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
import com.example.bmta_2022_semestralni_prace_hart.model.WarehouseItem
import com.example.bmta_2022_semestralni_prace_hart.model.WarehouseItemNotFoundException
import org.json.JSONException
import com.example.bmta_2022_semestralni_prace_hart.viewmodel.Warehouse
import java.io.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var warehouse: Warehouse
    private val mapper =
        jacksonObjectMapper() //knihovna pro serializaci a deserializaci objektů do JSON

    //Obsluha po dokončení aktivity skenování kódu
    private val barcodeLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(this@MainActivity, "Načítání zrušeno", Toast.LENGTH_LONG).show()
            } else {
                val item: WarehouseItem
                try {
                    item = warehouse.findWarehouseItem(result.contents)
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
                } catch (e: WarehouseItemNotFoundException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Neznámá položka: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                writeToFile(makeJsonString(), applicationContext) //Uložení do souboru
            }
        }

    //Obsluha po dokončení aktivity definovaní nového itemu
    private val defineItemIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val newItem =
                    result.data?.getSerializableExtra("newItem") as WarehouseItem //ponecháno deprecated z důvodu kompatibility s API 26
                try {
                    warehouse.defineNewWarehouseItem(newItem)
                    writeToFile(makeJsonString(), applicationContext) //Uložení do souboru
                    Toast.makeText(
                        this@MainActivity,
                        "Přidána položka " + newItem.description,
                        Toast.LENGTH_LONG
                    ).show()
                } catch (ex: java.lang.IllegalArgumentException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Chyba: Položka s tímto kódem již existuje!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    //Obsluha po dokončení aktivity editace itemu
    private val editItemIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val editedItem =
                    result.data?.getSerializableExtra("editedItem") as WarehouseItem //ponecháno deprecated z důvodu kompatibility s API 26
                try {
                    warehouse.allItems[editedItem.id]?.description = editedItem.description
                    warehouse.allItems[editedItem.id]?.numberOfBorrowings =
                        editedItem.numberOfBorrowings
                    writeToFile(makeJsonString(), applicationContext) //Uložení do souboru
                    Toast.makeText(
                        this@MainActivity,
                        "Editována položka " + editedItem.description,
                        Toast.LENGTH_LONG
                    ).show()
                } catch (ex: java.lang.IllegalArgumentException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Chyba: Položka s tímto kódem již existuje!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    //Obsluha po dokončení aktivity odebírání itemu
    private val removeItemIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val codeToRemove =
                    result.data?.getStringExtra("codeToRemove")
                if (codeToRemove != null) {
                    try {
                        val removed = warehouse.removeWarehouseItem(codeToRemove)
                        writeToFile(makeJsonString(), applicationContext) //Uložení do souboru
                        Toast.makeText(
                            this@MainActivity,
                            "Ze skladu smazána položka: " + removed?.description,
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //binding prvků z layoutu
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //přizpůsobení horní lišty aplikace modernímu vzhledu
        supportActionBar?.hide()
        this.window.statusBarColor =
            this.resources.getColor(R.color.blue_darker2, null)

        //načítání uložených dat, předání do factory (vytváření instance pro mvvm)
        val savedItemsJson = readFromFile(applicationContext)
        try {
            val factory = WarehouseFactory(savedItemsJson)
            warehouse = ViewModelProvider(this, factory)[Warehouse::class.java]
        } catch (ex: JSONException) {
            Toast.makeText(
                this@MainActivity,
                ex.message,
                Toast.LENGTH_LONG
            ).show()
            this.finishAffinity()
        }

        //nastavení OnClick tlačítek
        binding.buttonScan.setOnClickListener { buttonScanOnClick() }
        binding.buttonSettings.setOnClickListener { settingsButtonOnClick() }
    }

    //Obsluha eventu stisknutí tlačítka buttonScan
    private fun buttonScanOnClick() {
        val options = ScanOptions()
        options.setPrompt("Stiskni tlačítko volume nahoru pro rozsvícení baterky")
        options.setBeepEnabled(false)
        options.setOrientationLocked(true)
        options.captureActivity = CaptureAct().javaClass
        barcodeLauncher.launch(options)
    }

    //Obsluha eventu stisknutí tlačítka buttonSettings
    private fun settingsButtonOnClick() {
        // Initializing the popup menu and giving the reference as current context
        val popupMenu = PopupMenu(this@MainActivity, binding.buttonSettings)

        // Inflating popup menu from popup_menu.xml file
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem -> //Vyvolej obsluhu, podle druhu menuItemu
            when (menuItem.titleCondensed) {
                "showAllItems" -> {
                    val i = Intent(this, AllItemsActivity::class.java)
                    i.putExtra("warehouse", warehouse)
                    startActivity(i)
                }
                "showBorrowedItems" -> {
                    val i = Intent(this, BorrowedItemsActivity::class.java)
                    i.putExtra("warehouse", warehouse)
                    startActivity(i)
                }
                "defineItem" -> {
                    defineItemIntentLauncher.launch(Intent(this, NewItemActivity::class.java))
                }
                "removeItem" -> {
                    val i = Intent(this, RemoveItemActivity::class.java)
                    i.putExtra("warehouse", warehouse)
                    removeItemIntentLauncher.launch(i)
                }
                "editItem" -> {
                    val i = Intent(this, EditItemActivity::class.java)
                    i.putExtra("warehouse", warehouse)
                    editItemIntentLauncher.launch(i)
                }
            }
            true
        }
        // Showing the popup menu
        popupMenu.show()
    }

    //vytvoření JSON stringu pro uložení definovaných itemů a kódů itemů, které jsou vypůjčené
    private fun makeJsonString(): String {
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

    //zápis do souboru
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

    //načtení ze souboru
    private fun readFromFile(context: Context): String {
        var ret = ""
        try {
            val inputStream: InputStream = context.openFileInput("config.txt")
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var receiveString: String?
            val stringBuilder = java.lang.StringBuilder()
            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append("\n").append(receiveString)
            }
            inputStream.close()
            ret = stringBuilder.toString()

        } catch (e: FileNotFoundException) {
            Log.e("login activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("login activity", "Can not read file: $e")
        }
        return ret
    }

}
