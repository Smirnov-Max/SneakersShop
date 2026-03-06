package com.example.sneakersshop.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.sneakersshop.R
import com.example.sneakersshop.data.RetrofitInstance
import com.example.sneakersshop.data.service.ProfileCreationRequest
import com.example.sneakersshop.data.service.ProfileDto
import com.example.sneakersshop.ui.theme.PrimaryBlue
import com.example.sneakersshop.ui.theme.TextFieldBackground
import com.example.sneakersshop.ui.theme.TextMain
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileScreen(
    navController: NavHostController,
    userId: String,
    accessToken: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Состояния для полей профиля
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") } // сырой номер (только цифры)
    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    // Состояния загрузки и ошибок
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // ---------- Камера для фото ----------
    val tmpImageUri = remember {
        val file = File(context.cacheDir, "profile_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) avatarUri = tmpImageUri
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(tmpImageUri)
        else Toast.makeText(context, "Нужен доступ к камере", Toast.LENGTH_SHORT).show()
    }

    fun launchCamera() {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        if (ok) cameraLauncher.launch(tmpImageUri) else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // ---------- Загрузка профиля (и создание при необходимости) ----------
    LaunchedEffect(userId, accessToken) {
        if (userId.isBlank() || accessToken.isBlank()) {
            errorText = "Ошибка авторизации"
            return@LaunchedEffect
        }
        isLoading = true
        try {
            val service = RetrofitInstance.userManagementService
            val response = service.getProfile(
                authHeader = "Bearer $accessToken",
                userIdFilter = "eq.$userId"
            )

            if (response.isSuccessful) {
                val list = response.body() ?: emptyList()
                if (list.isNotEmpty()) {
                    val profile = list.first()
                    firstName = profile.firstname.orEmpty()
                    lastName = profile.lastname.orEmpty()
                    address = profile.address.orEmpty()
                    phone = profile.phone.orEmpty()
                    profile.photo?.let { avatarUri = Uri.parse(it) }
                    // Если есть photo, можно загрузить, но пока оставим
                } else {
                    // Профиль не найден – создаём новый
                    val createResponse = service.createProfile(
                        authHeader = "Bearer $accessToken",
                        profile = ProfileCreationRequest(
                            user_id = userId,
                            firstname = "",
                            lastname = "",
                            address = "",
                            phone = "",
                            photo = null
                        )
                    )
                    if (createResponse.isSuccessful) {
                        // Оставляем поля пустыми
                        firstName = ""
                        lastName = ""
                        address = ""
                        phone = ""
                    } else {
                        errorText = "Не удалось создать профиль: ${createResponse.code()}"
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PROFILE", "Ошибка загрузки профиля: ${response.code()}, тело: $errorBody")
                errorText = "Ошибка загрузки профиля: ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e("PROFILE", "Исключение при загрузке профиля", e)
            errorText = "Не удалось загрузить профиль: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Форматирование номера телефона для отображения
    fun formatPhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when (digits.length) {
            11 -> "+7 (${digits.substring(1, 4)}) ${digits.substring(4, 7)}-${digits.substring(7, 9)}-${digits.substring(9, 11)}"
            10 -> "+7 (${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6, 8)}-${digits.substring(8, 10)}"
            else -> raw // если не 10 или 11 цифр, показываем как есть
        }
    }

    // Scaffold с нижней навигацией
    Scaffold(
        containerColor = Color.White,
        bottomBar = { BottomBar(navController = navController, currentRoute = "profile") }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Верхняя панель с заголовком и кнопкой редактирования
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Профиль",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                                .clickable {
                                    // Преобразование пустых полей в плейсхолдер "_"
                                    val safeFirstName = if (firstName.isBlank()) "_" else firstName
                                    val safeLastName = if (lastName.isBlank()) "_" else lastName
                                    val safeAddress = if (address.isBlank()) "_" else address
                                    val safePhone = if (phone.isBlank()) "_" else phone
                                    navController.navigate("editProfile/$safeFirstName/$safeLastName/$safeAddress/$safePhone")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Аватар
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable { launchCamera() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(model = avatarUri),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_profile),
                                contentDescription = "Placeholder",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Имя и фамилия
                    Text(
                        text = "$firstName $lastName".ifBlank { "Не указано" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMain
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Карточка со штрих-кодом (декоративный элемент)
                    BarcodeCard()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Поля профиля (только для чтения)
                    ProfileFieldReadOnly("Имя", firstName)
                    ProfileFieldReadOnly("Фамилия", lastName)
                    ProfileFieldReadOnly("Адрес", address)
                    ProfileFieldReadOnly("Телефон", formatPhone(phone))

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Диалог ошибки
    if (errorText != null) {
        AlertDialog(
            onDismissRequest = { errorText = null },
            title = { Text("Ошибка") },
            text = { Text(errorText ?: "") },
            confirmButton = {
                TextButton(onClick = { errorText = null }) { Text("OK") }
            }
        )
    }
}

// Компонент для отображения поля только для чтения
@Composable
fun ProfileFieldReadOnly(title: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color(0xFF888888),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TextFieldBackground)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifBlank { "—" },
                fontSize = 16.sp,
                color = TextMain
            )
        }
    }
}

// Декоративная карточка со штрих-кодом (можно оставить)
@Composable
fun BarcodeCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Открыть",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.rotate(-90f),
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_barcode),
                contentDescription = "Barcode",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}