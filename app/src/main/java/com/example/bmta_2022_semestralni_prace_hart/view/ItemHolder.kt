package com.example.bmta_2022_semestralni_prace_hart.view

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bmta_2022_semestralni_prace_hart.R
import com.example.bmta_2022_semestralni_prace_hart.model.WarehouseItem

class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var itemDescription: TextView?
    private var itemCode: TextView?
    private var itemLastSeen: TextView?
    private var itemBorrowings: TextView?

    init {
        itemDescription = view.findViewById(R.id.itemRowDescription)
        itemCode = view.findViewById(R.id.itemRowID)
        itemLastSeen = view.findViewById(R.id.itemRowLastSeen)
        itemBorrowings = view.findViewById(R.id.itemRowBorrowings)
    }

    fun setItemData(item: WarehouseItem) {
        itemDescription?.text = item.description
        itemCode?.text = item.id
        itemLastSeen?.text = item.lastSeen
        itemBorrowings?.text = item.numberOfBorrowings.toString()
    }
}