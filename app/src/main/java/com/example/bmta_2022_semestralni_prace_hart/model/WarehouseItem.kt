package com.example.bmta_2022_semestralni_prace_hart.model

import java.io.Serializable
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class WarehouseItem(
    val id: String = "",
    var description: String = "",
    var numberOfBorrowings: Int = 0,
    var lastSeen: String = DateTimeFormatter
        .ofPattern("dd. MM. yyyy HH:mm")
        .withZone(ZoneOffset.ofHours(1))
        .format(Instant.now())) : Serializable
