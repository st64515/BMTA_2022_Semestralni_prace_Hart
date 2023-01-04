package com.example.bmta_2022_semestralni_prace_hart.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityAllItemsBinding
import viewmodel.Warehouse

class AllItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllItemsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.hide()
        this.window.statusBarColor =
            this.resources.getColor(com.example.bmta_2022_semestralni_prace_hart.R.color.blue_darker2)

        binding.itemRecycler.layoutManager= LinearLayoutManager(this)
        var warehouse = intent.getSerializableExtra("warehouse") as Warehouse

        if (!warehouse.allItemsList().isEmpty()) {
            binding.itemRecycler.adapter = ItemAdapter(warehouse.allItemsList())
        } else {
            binding.headline.text = getString(R.string.no_items_to_show)
        }

        binding.itemRecycler.adapter?.notifyDataSetChanged()
    }
}