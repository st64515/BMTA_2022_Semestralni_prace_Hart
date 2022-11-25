package com.example.bmta_2022_semestralni_prace_hart

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonScan.setOnClickListener()
        {
            scanCode()
        }
    }

    private fun scanCode() {
        val options = ScanOptions()
        options.setPrompt("Stiskni tlačítko volume nahoru pro rozsvícení baterky")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.captureActivity = CaptureAct().javaClass
        barcodeLauncher.launch(options)


    }


    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this@MainActivity, "Scanned: " + result.contents, Toast.LENGTH_LONG)
                .show()
        }
    }


}