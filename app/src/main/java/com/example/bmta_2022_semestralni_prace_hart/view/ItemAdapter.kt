package com.example.bmta_2022_semestralni_prace_hart.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.model.WarehouseItem

class ItemAdapter (private val itemsList : List<WarehouseItem>) : RecyclerView.Adapter<ItemHolder> () {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.setItemData(itemsList[position])
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }
}