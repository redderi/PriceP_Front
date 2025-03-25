package com.redderi.pricep.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.redderi.pricep.R
import com.redderi.pricep.utils.ChangeLocale
import com.redderi.pricep.utils.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    userPreferences: UserPreferences
) {
    val darkMode by userPreferences.darkModeFlow.collectAsState(initial = false)
    val selectedLanguage by userPreferences.languageFlow.collectAsState(initial = "Русский")
    val coroutineScope = rememberCoroutineScope()
    var expandedLanguage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    ChangeSystemBarsColor(
        statusBarColor = MaterialTheme.colorScheme.primaryContainer,
        navBarColor = MaterialTheme.colorScheme.primaryContainer
    )

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp)
            ) {
                IconButton(onClick = { navController.navigate("main") }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            SettingsSwitch(
                text = stringResource(R.string.dark_mode),
                checked = darkMode,
                onCheckedChange = { isChecked ->
                    coroutineScope.launch {
                        userPreferences.saveDarkMode(isChecked)
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.language),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box {
                    Text(
                        text = selectedLanguage,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.clickable { expandedLanguage = true }
                    )
                    DropdownMenu(
                        expanded = expandedLanguage,
                        onDismissRequest = { expandedLanguage = false }
                    ) {
                        listOf("Русский", "English").forEach { language ->
                            DropdownMenuItem(
                                text = { Text(text = language) },
                                onClick = {
                                    coroutineScope.launch {
                                        userPreferences.saveSelectedLanguage(language)
                                    }
                                    ChangeLocale(context, if (language == "Русский") "ru" else "en")
                                }
                            )
                        }
                    }
                }
            }

            SettingsButton(
                text = stringResource(R.string.reset_settings),
                onClick = {
                    coroutineScope.launch {
                        userPreferences.resetSettings()
                    }
                }
            )

            SettingsText(
                text = stringResource(R.string.app_version),
                value = "1.0.0"
            )
        }
    }
}

@Composable
fun SettingsSwitch(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.SettingsBackupRestore,
                contentDescription = "Reset",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SettingsText(text: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}