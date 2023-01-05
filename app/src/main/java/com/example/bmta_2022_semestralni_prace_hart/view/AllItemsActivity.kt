package com.example.bmta_2022_semestralni_prace_hart.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityAllItemsBinding
import com.example.bmta_2022_semestralni_prace_hart.viewmodel.Warehouse

class AllItemsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAllItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        this.window.statusBarColor =
            this.resources.getColor(R.color.blue_darker2)

        binding.itemRecycler.layoutManager= LinearLayoutManager(this)
        val warehouse = intent.getSerializableExtra("warehouse") as Warehouse

        if (warehouse.allItemsList().isNotEmpty()) {
            binding.itemRecycler.adapter = ItemAdapter(warehouse.allItemsList())
        } else {
            binding.headline.text = getString(R.string.no_items_to_show)
        }

        binding.itemRecycler.adapter?.notifyDataSetChanged()
    }
}