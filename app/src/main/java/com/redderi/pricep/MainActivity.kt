package com.redderi.pricep

import PhotoScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.redderi.pricep.screens.HistoryScreen
import com.redderi.pricep.screens.MainScreen
import com.redderi.pricep.screens.SettingsScreen
import com.redderi.pricep.ui.theme.PricepTheme
import com.redderi.pricep.utils.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userPreferences = UserPreferences(LocalContext.current.applicationContext)
            MainApp(userPreferences)
        }
    }
}

@Composable
fun MainApp(userPreferences: UserPreferences) {
    val navController: NavHostController = rememberNavController()
    var darkMode by remember { mutableStateOf<Boolean?>(null) }
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userPreferences.darkModeFlow.collect { mode ->
            darkMode = mode
        }
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else if (darkMode == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        PricepTheme(
            darkTheme = darkMode == true,
            dynamicColor = false
        ) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("main") { MainScreen(navController, userPreferences) }
                    composable("photo_screen") { PhotoScreen(navController, userPreferences) }
                    composable("settings_screen") {
                        SettingsScreen(
                            navController,
                            userPreferences
                        )
                    }
                    composable("history_screen") { HistoryScreen(navController, userPreferences) }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var showLogo by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1000)
        showLogo = false
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (showLogo) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp)
            )
        } else {
            CircularProgressIndicator()
        }
    }
}