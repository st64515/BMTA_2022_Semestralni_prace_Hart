package com.example.bmta_2022_semestralni_prace_hart.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.databinding.ActivityBorrowedItemsBinding
import viewmodel.Warehouse

class BorrowedItemsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBorrowedItemsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBorrowedItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.hide()
        this.window.statusBarColor =
            this.resources.getColor(com.example.bmta_2022_semestralni_prace_hart.R.color.blue_darker2)

        binding.itemRecycler.layoutManager = LinearLayoutManager(this)
        var warehouse = intent.getSerializableExtra("warehouse") as Warehouse

        if (!warehouse.borrowedItemsList().isEmpty()) {
            binding.itemRecycler.adapter = ItemAdapter(warehouse.borrowedItemsList())
        } else {
            binding.headline.text = getString(R.string.no_items_to_show)
        }

        binding.itemRecycler.adapter?.notifyDataSetChanged()
    }
}