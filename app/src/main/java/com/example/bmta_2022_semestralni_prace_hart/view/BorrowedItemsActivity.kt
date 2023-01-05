package com.example.bmta_2022_semestralni_prace_hart.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityBorrowedItemsBinding
import com.example.bmta_2022_semestralni_prace_hart.viewmodel.Warehouse

class BorrowedItemsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBorrowedItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        this.window.statusBarColor =
            this.resources.getColor(R.color.blue_darker2)

        binding.itemRecycler.layoutManager = LinearLayoutManager(this)
        val warehouse = intent.getSerializableExtra("warehouse") as Warehouse

        if (warehouse.borrowedItemsList().isNotEmpty()) {
            binding.itemRecycler.adapter = ItemAdapter(warehouse.borrowedItemsList())
        } else {
            binding.headline.text = getString(R.string.no_items_to_show)
        }

        binding.itemRecycler.adapter?.notifyDataSetChanged()
    }
}