package com.redderi.pricep.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.redderi.pricep.R
import com.redderi.pricep.components.*
import com.redderi.pricep.network.RetrofitClient
import com.redderi.pricep.network.TextRequest
import com.redderi.pricep.network.TextResponse
import com.redderi.pricep.utils.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ChangeSystemBarsColor(statusBarColor: Color, navBarColor: Color) {
    val context = LocalContext.current

    if (context is ComponentActivity) {
        val window = context.window
        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navBarColor.toArgb()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val decorView = window.decorView
            decorView.windowInsetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun formatServerResponse(response: TextResponse): Pair<AnnotatedString, Map<String, String>> {
    val urlMap = mutableMapOf<String, String>()
    val annotatedText = buildAnnotatedString {
        response.product_info?.forEachIndexed { index, product ->
            append("${product.name}\n")
            append("${product.price}\n")

            append("🔗\n\n")

            urlMap["icon_$index"] = product.url
        }
    }
    return Pair(annotatedText, urlMap)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, userPreferences: UserPreferences) {
    val text = remember { mutableStateOf("") }
    val serverResponse = remember { mutableStateOf(Pair(AnnotatedString(""), emptyMap<String, String>())) }
    val isLoading = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val answerTextState = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("answerText", null)
        ?.collectAsState()

    val answerText = answerTextState?.value

    LaunchedEffect(answerText) {
        answerText?.let { response ->
            text.value = response
            sendTextToServer(text.value, context, serverResponse, isLoading)
        }
    }

    ChangeSystemBarsColor(
        statusBarColor = MaterialTheme.colorScheme.primaryContainer,
        navBarColor = MaterialTheme.colorScheme.primaryContainer
    )

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        AppBar(
            expanded = expanded,
            leftIcons = listOf(
                AppBarIcon(
                    icon = Icons.Filled.History,
                    contentDescription = "История",
                    onClick = { navController.navigate("history_screen") }
                ),
                AppBarIcon(
                    icon = Icons.Filled.Star,
                    contentDescription = "Избранное",
                    onClick = { }
                )
            ),
            rightIcons = listOf(
                AppBarIcon(
                    icon = Icons.Filled.Settings,
                    contentDescription = "Настройки",
                    onClick = { navController.navigate("settings_screen") }
                )
            ),
            title = AppBarTitle(
                text = "PriceP".uppercase(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ),
            dropdownItems = listOf(
                AppBarDropdownItem(
                    text = stringResource(R.string.about_app_info_text),
                    onClick = { expanded.value = false }
                )
            )
        )

        Column(modifier = Modifier.weight(1f)) {
            TextField(
                value = text.value,
                onValueChange = { text.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.describe_product),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.background
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        sendTextToServer(text.value, context, serverResponse, isLoading)
                    }
                )
            )

            if (isLoading.value) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(4f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                }
            } else if (serverResponse.value.first.text.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(4f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        val textParts = serverResponse.value.first.text.split("🔗")
                        Column {
                            textParts.forEachIndexed { index, part ->
                                Text(
                                    text = part,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (index < textParts.size - 1) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo_ozon),
                                        contentDescription = "Product link",
                                        modifier = Modifier
                                            .fillMaxSize(0.4f)
                                            .clickable {
                                                serverResponse.value.second["icon_$index"]?.let { url ->
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                    context.startActivity(intent)
                                                }
                                            }
                                            .padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        BottomBar(
            leftIcon = BottomBarIcon(
                icon = Icons.Filled.AspectRatio,
                contentDescription = "Режим работы",
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                label = stringResource(R.string.mode),
                onClick = { /* Режим работы */ }
            ),
            centerIcon = BottomBarIcon(
                icon = Icons.Filled.Mic,
                contentDescription = "Микрофон",
                backgroundColor = MaterialTheme.colorScheme.secondary,
                size = 100.dp,
                onClick = { /* Микрофон */ }
            ),
            rightIcon = BottomBarIcon(
                icon = Icons.Filled.CameraAlt,
                contentDescription = "Камера",
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                label = stringResource(R.string.camera),
                onClick = { navController.navigate("photo_screen") }
            )
        )
    }
}

fun sendTextToServer(
    text: String,
    context: Context,
    serverResponse: MutableState<Pair<AnnotatedString, Map<String, String>>>,
    isLoading: MutableState<Boolean>
) {
    val apiService = RetrofitClient.apiService
    val call = apiService.sendText(TextRequest(text))

    isLoading.value = true

    call.enqueue(object : Callback<TextResponse> {
        override fun onResponse(call: Call<TextResponse>, response: Response<TextResponse>) {
            isLoading.value = false
            if (response.isSuccessful) {
                serverResponse.value = response.body()?.let { formatServerResponse(it) }
                    ?: Pair(AnnotatedString("Error: empty response"), emptyMap())
            } else {
                Toast.makeText(context, "Error to send", Toast.LENGTH_LONG).show()
            }
        }

        override fun onFailure(call: Call<TextResponse>, t: Throwable) {
            isLoading.value = false
            Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
        }
    })
}