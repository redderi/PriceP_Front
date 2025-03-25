package com.redderi.pricep.database

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {

    val allHistoryItems: Flow<List<HistoryItem>> = historyDao.getAllHistoryItems()

    suspend fun insert(historyItem: HistoryItem) {
        historyDao.insert(historyItem)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}