import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.redderi.pricep.network.DefineImageResponse
import com.redderi.pricep.network.RetrofitClient
import com.redderi.pricep.screens.ChangeSystemBarsColor
import com.redderi.pricep.utils.UserPreferences
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.InputStream

@Composable
fun PhotoScreen(navController: NavController, userPreferences: UserPreferences) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPermissionGranted by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }

    val cameraPermissionStatus = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isPermissionGranted = isGranted
    }

    LaunchedEffect(cameraPermissionStatus) {
        if (cameraPermissionStatus == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    ChangeSystemBarsColor(
        statusBarColor = Color.Black,
        navBarColor = Color.Black
    )

    if (isPermissionGranted) {
        val imageCapture = remember { ImageCapture.Builder().build() }
        val galleryLauncher = rememberGalleryLauncher(
            context = context,
            navController = navController,
            updateProgress = { showProgress = it },
            sendImageToApi = ::sendImageToApi
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate("main") }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Вернуться",
                            tint = Color.White
                        )
                    }
                }

                CameraPreview(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    imageCapture = imageCapture,
                    modifier = Modifier.weight(2f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.Black)
                        .padding(start = 32.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .size(70.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.LightGray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "Галерея",
                            modifier = Modifier.scale(2f) // Оптимальный размер иконки
                        )
                    }

                    Button(
                        onClick = {
                            capturePhoto(
                                context = context,
                                lifecycleOwner = lifecycleOwner,
                                navController = navController,
                                imageCapture = imageCapture,
                                showProgress = showProgress,
                                updateProgress = { showProgress = it }
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .size(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.LightGray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Camera,
                            contentDescription = "Камера",
                            modifier = Modifier.scale(2f)
                        )
                    }
                }
            }

            if (showProgress) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(50.dp),
                        color = Color.White
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Для использования камеры необходимо разрешение.",
                color = Color.White
            )
        }
    }
}

@Composable
fun rememberGalleryLauncher(
    context: Context,
    navController: NavController,
    updateProgress: (Boolean) -> Unit,
    sendImageToApi: (Context, ByteArray, NavController, (Boolean) -> Unit) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri -> handleGalleryImageResult(uri, context, navController, updateProgress, sendImageToApi) }
)

private fun handleGalleryImageResult(
    uri: Uri?,
    context: Context,
    navController: NavController,
    updateProgress: (Boolean) -> Unit,
    sendImageToApi: (Context, ByteArray, NavController, (Boolean) -> Unit) -> Unit
) {
    uri?.let { imageUri ->
        updateProgress(true)
        try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                val byteArray = inputStream.readBytes()
                if (byteArray.isNotEmpty()) {
                    sendImageToApi(context, byteArray, navController, updateProgress)
                } else {
                    showErrorToast(context, "Ошибка чтения изображения")
                    updateProgress(false)
                }
            }
        } catch (e: Exception) {
            showErrorToast(context, "Ошибка обработки изображения")
            Log.e("GalleryHandler", "Ошибка обработки изображения", e)
            updateProgress(false)
        }
    }
}

private fun showErrorToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    imageCapture: ImageCapture,
    modifier: Modifier = Modifier
) {
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    ) { previewView ->
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка при подключении камеры: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun capturePhoto(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    navController: NavController,
    imageCapture: ImageCapture,
    showProgress: Boolean,
    updateProgress: (Boolean) -> Unit
) {
    val photoFile = File.createTempFile(
        "temp_image",
        ".jpg",
        context.cacheDir
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                try {
                    val byteArray = photoFile.readBytes()
                    updateProgress(true)
                    sendImageToApi(context, byteArray, navController, updateProgress)
                } catch (e: Exception) {
                    showErrorToast(context, "Ошибка обработки фото")
                    Log.e("PhotoCapture", "Ошибка обработки фото", e)
                } finally {
                    photoFile.delete()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                showErrorToast(context, "Ошибка при захвате фото")
                Log.e("PhotoCapture", "Ошибка при захвате фото", exception)
            }
        }
    )
}

fun sendImageToApi(
    context: Context,
    imageBytes: ByteArray,
    navController: NavController,
    updateProgress: (Boolean) -> Unit
) {
    val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
    val imagePart = MultipartBody.Part.createFormData(
        "image",
        "photo.jpg",
        requestFile
    )
    val call = RetrofitClient.apiService.defineImage(imagePart)

    call.enqueue(object : Callback<DefineImageResponse> {
        override fun onResponse(call: Call<DefineImageResponse>, response: Response<DefineImageResponse>) {
            updateProgress(false)

            if (response.isSuccessful) {
                val answerText = response.body()?.answer_text ?: "Ответ пуст"
                Toast.makeText(context, "Ответ от сервера: $answerText", Toast.LENGTH_SHORT).show()

                navController.previousBackStackEntry?.savedStateHandle?.set("answerText", answerText)

                navController.popBackStack()
            } else {
                Toast.makeText(context, "Ошибка при отправке фото", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<DefineImageResponse>, t: Throwable) {
            updateProgress(false)

            Toast.makeText(context, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            Log.e("PhotoScreen", "Ошибка сети", t)
        }
    })
}