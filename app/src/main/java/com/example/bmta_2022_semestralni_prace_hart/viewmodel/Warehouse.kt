package viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import model.WarehouseItem
import model.WarehouseItemNotFoundException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Warehouse(savedItemsJson: String) : ViewModel(), Serializable {
    var allItems = mutableMapOf<String, WarehouseItem>()
    var borrowedItems = mutableMapOf<String, WarehouseItem>()

    fun defineNewWarehouseItem(wItem: WarehouseItem) {
        if (allItems.containsKey(wItem.id)) {
            throw IllegalArgumentException(wItem.id)
        }
        allItems[wItem.id] = wItem
    }

    fun borrowWarehouseItem(wItemID: String) {
        val wItem = allItems[wItemID]
        if (wItem is WarehouseItem && !borrowedItems.containsKey(wItemID)) {
            wItem.lastSeen = DateTimeFormatter
                .ofPattern("dd. MM. yyyy HH:mm")
                .withZone(ZoneOffset.ofHours(1))
                .format(Instant.now())
            borrowedItems[wItem.id] = wItem
        } else {
            throw WarehouseItemNotFoundException(wItemID)
        }
    }

    fun findWarehouseItem(wItemID: String): WarehouseItem {
        val wItem = allItems[wItemID]
        if (wItem is WarehouseItem) {
            return wItem
        } else {
            throw WarehouseItemNotFoundException(wItemID)
        }
    }

    fun returnWarehouseItem(wItemID: String) {
        var wItem = borrowedItems[wItemID]
        if (wItem is WarehouseItem) {
            wItem.numberOfBorrowings++
            wItem.lastSeen = DateTimeFormatter
                .ofPattern("dd. MM. yyyy HH:mm")
                .withZone(ZoneOffset.ofHours(1))
                .format(Instant.now())
            borrowedItems.remove(wItemID)
        } else {
            throw WarehouseItemNotFoundException(wItemID)
        }
    }

    fun removeWarehouseItem(wItemID: String) {
        val wItem = allItems[wItemID]
        if (wItem is WarehouseItem) {
            allItems.remove(wItemID)
            if (borrowedItems.containsKey(wItemID)) {
                borrowedItems.remove(wItemID)
            }
        } else {
            throw WarehouseItemNotFoundException(wItemID)
        }
    }

    fun allItemsList(): ArrayList<WarehouseItem> {
        var list = ArrayList<WarehouseItem>()
        for (item in allItems) {
            list.add(item.value)
        }
        return list
    }

    fun borrowedItemsList(): ArrayList<WarehouseItem> {
        var list = ArrayList<WarehouseItem>()
        for (item in borrowedItems) {
            list.add(item.value)
        }
        return list
    }

    init {
        var allItemsArray = parseJsonArray(savedItemsJson, "allItems")
        var borrowedCodesArray = parseJsonArray(savedItemsJson, "borrowedCodes")
        loadAllItems(allItemsArray)
        loadBorrowedItems(borrowedCodesArray)
    }

    fun parseJsonArray(settingsJson: String, item: String): JSONArray? {
        try {
            val jObj = JSONObject(settingsJson)
            return jObj.getJSONArray(item)
        } catch (ex: JSONException) {
            Log.i("JsonParser array", "unexpected JSON exception", ex)
            return null
        }
    }

    private fun loadAllItems(allItems: JSONArray?) {
        var warehouseItem: WarehouseItem
        allItems?.let {
            for (i in 0 until allItems.length()) {
                val warehouseItemType = allItems.getJSONObject(i)
                warehouseItem = WarehouseItem(
                    id = warehouseItemType.getString("id"),
                    description = warehouseItemType.getString("description"),
                    numberOfBorrowings = warehouseItemType.getInt("numberOfBorrowings"),
                    lastSeen = warehouseItemType.getString("lastSeen"),
                )
                try {
                    this.defineNewWarehouseItem(warehouseItem)
                } catch (ex: java.lang.IllegalArgumentException) {
                    throw JSONException("Chyba při načítání uložených položek")
                }
            }
        }
    }

    private fun loadBorrowedItems(borrowedCodes: JSONArray?) {
        var warehouseItem: WarehouseItem
        borrowedCodes?.let {
            for (i in 0 until borrowedCodes.length()) {
                try {
                    borrowWarehouseItem(borrowedCodes[i].toString())
                } catch (ex: WarehouseItemNotFoundException) {
                    throw JSONException("Chyba při načítání vypůjčených položek")
                }
            }
        }
    }

    fun borrowedCodesList(): ArrayList<String> {
        var codes = ArrayList<String>()
        for (item in borrowedItems)
        {
            codes.add(item.key)
        }
        return codes
    }


}