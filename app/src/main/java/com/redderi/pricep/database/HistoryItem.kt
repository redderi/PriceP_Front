package com.redderi.pricep.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestText: String,
    val responseText: String,
    val timestamp: Long = System.currentTimeMillis()
)