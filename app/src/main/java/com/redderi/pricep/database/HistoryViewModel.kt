package com.redderi.pricep.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HistoryViewModel(private val historyDao: HistoryDao) : ViewModel() {

    val allHistoryItems: Flow<List<HistoryItem>> = historyDao.getAllHistoryItems()

    fun insert(historyItem: HistoryItem) = viewModelScope.launch {
        historyDao.insert(historyItem)
    }

    fun clearHistory() = viewModelScope.launch {
        historyDao.clearHistory()
    }
}