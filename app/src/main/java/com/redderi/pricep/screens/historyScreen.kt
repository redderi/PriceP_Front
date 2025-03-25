package com.redderi.pricep.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.redderi.pricep.database.AppDatabase
import com.redderi.pricep.database.HistoryItem
import com.redderi.pricep.database.HistoryViewModel
import com.redderi.pricep.database.HistoryViewModelFactory
import com.redderi.pricep.utils.UserPreferences
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun HistoryScreen(navController: NavController, userPreferences: UserPreferences) {
    val context = LocalContext.current

    val database = remember {
        try {
            AppDatabase.getDatabase(context.applicationContext)
        } catch (e: Exception) {
            null
        }
    }

    if (database == null) {
        ErrorScreen(message = "Ошибка инициализации базы данных")
        return
    }

    val historyDao = database.historyDao()
    val historyViewModel: HistoryViewModel =
        viewModel(factory = HistoryViewModelFactory(historyDao))

    val historyItems by historyViewModel.allHistoryItems.collectAsState(initial = emptyList())

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "История запросов",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                historyItems.isEmpty() -> {
                    EmptyHistoryScreen()
                }
                else -> {
                    HistoryList(historyItems, navController)
                }
            }
        }
    }
}

@Composable
fun HistoryList(items: List<HistoryItem>, navController: NavController) {
    LazyColumn {
        items(items) { item ->
            HistoryItemCard(item, navController)
        }
    }
}

@Composable
fun HistoryItemCard(historyItem: HistoryItem, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Запрос: ${historyItem.requestText}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Ответ: ${historyItem.responseText}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Дата: ${SimpleDateFormat("dd.MM.yyyy HH:mm").format(Date(historyItem.timestamp))}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    try {
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "historyItem",
                            historyItem.copy()
                        )
                        navController.popBackStack()
                    } catch (e: Exception) {
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Использовать этот запрос")
            }
        }
    }
}

@Composable
fun EmptyHistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("История запросов пуста")
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* retry logic */ }) {
                Text("Попробовать снова")
            }
        }
    }
}