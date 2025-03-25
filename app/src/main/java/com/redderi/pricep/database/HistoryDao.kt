package com.redderi.pricep.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(historyItem: HistoryItem)

    @Query("SELECT * FROM history_items ORDER BY timestamp DESC")
    fun getAllHistoryItems(): Flow<List<HistoryItem>>

    @Query("DELETE FROM history_items")
    suspend fun clearHistory()
}