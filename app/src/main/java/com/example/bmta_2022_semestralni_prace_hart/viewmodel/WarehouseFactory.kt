package com.example.bmta_2022_semestralni_prace_hart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import viewmodel.Warehouse

class WarehouseFactory(private val savedItemsJson: String) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(Warehouse::class.java)) {
            return Warehouse(savedItemsJson) as T
        }
        throw IllegalArgumentException("Unknown Viewmodel class")
    }
}