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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.sneakersshop.R
import com.example.sneakersshop.data.RetrofitInstance
import com.example.sneakersshop.ui.theme.PrimaryBlue
import com.example.sneakersshop.ui.theme.TextFieldBackground
import com.example.sneakersshop.ui.theme.TextMain
import com.example.sneakersshop.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun EditProfileScreen(
    navController: NavHostController,
    initialFirstName: String,
    initialLastName: String,
    initialAddress: String,
    initialPhone: String,
    userId: String,
    accessToken: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val actualFirstName = if (initialFirstName == "_") "" else initialFirstName
    val actualLastName = if (initialLastName == "_") "" else initialLastName
    val actualAddress = if (initialAddress == "_") "" else initialAddress
    val actualPhone = if (initialPhone == "_") "" else initialPhone

    var firstName by remember { mutableStateOf(actualFirstName) }
    var lastName by remember { mutableStateOf(actualLastName) }
    var address by remember { mutableStateOf(actualAddress) }
    var phone by remember { mutableStateOf(actualPhone) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    // ---------- Камера для фото ----------
    val tmpImageUri = remember {
        val file = File(context.cacheDir, "edit_profile_photo.jpg")
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

    // ---------- Загрузка фото в Storage ----------
    suspend fun uploadAvatar(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val fileName = "avatar_${userId}_${System.currentTimeMillis()}.jpg"
            val part = MultipartBody.Part.createFormData("file", fileName, requestFile)

            val response = RetrofitInstance.userManagementService.uploadAvatar(
                authHeader = "Bearer $accessToken",
                fileName = fileName,
                file = part
            )

            if (response.isSuccessful) {
                // Формируем публичный URL (замените на ваш проект)
                "https://jggqwcnsxaqbimqmoune.supabase.co/storage/v1/object/public/avatars/$fileName"
            } else {
                Log.e("UPLOAD", "Ошибка загрузки: ${response.code()} ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("UPLOAD", "Исключение при загрузке", e)
            null
        }
    }

    val isAllFieldsFilled = firstName.isNotBlank() && lastName.isNotBlank() &&
            address.isNotBlank() && phone.isNotBlank()

    val saveButtonColor = if (isAllFieldsFilled) PrimaryBlue else Color(0xFF2B6B8B)

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Кнопка назад
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TextFieldBackground)
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Назад",
                    tint = TextMain
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Профиль",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Аватар
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable { launchCamera() }
                    .align(Alignment.CenterHorizontally),
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Изменить фото профиля",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = PrimaryBlue,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { launchCamera() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле "Имя"
            Text("Имя", fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
            StyledTextField(
                value = firstName,
                onValueChange = { firstName = it.filter { !it.isDigit() } },
                placeholder = "Иван",
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле "Фамилия"
            Text("Фамилия", fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
            StyledTextField(
                value = lastName,
                onValueChange = { lastName = it.filter { !it.isDigit() } },
                placeholder = "Иванов",
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле "Адрес"
            Text("Адрес", fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
            StyledTextField(
                value = address,
                onValueChange = { address = it.filter { !it.isDigit() } },
                placeholder = "ул. Пушкина, д. 1",
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле "Телефон"
            Text("Телефон", fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
            StyledTextField(
                value = phone,
                onValueChange = { phone = it.filter { char -> char.isDigit() } },
                placeholder = "+7 (999) 123-45-67",
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Кнопка "Сохранить"
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            var photoUrl: String? = null
                            if (avatarUri != null) {
                                photoUrl = uploadAvatar(avatarUri!!)
                                if (photoUrl == null) {
                                    errorMessage = "Не удалось загрузить фото"
                                    isLoading = false
                                    return@launch
                                }
                            }

                            val body = mutableMapOf(
                                "firstname" to firstName,
                                "lastname" to lastName,
                                "address" to address,
                                "phone" to phone
                            )
                            photoUrl?.let { body["photo"] = it }

                            val response = RetrofitInstance.userManagementService.updateProfile(
                                authHeader = "Bearer $accessToken",
                                userIdFilter = "eq.$userId",
                                body = body
                            )
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Профиль обновлен", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e("EDIT_PROFILE", "Ошибка ${response.code()}: $errorBody")
                                errorMessage = "Ошибка сохранения: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            Log.e("EDIT_PROFILE", "Исключение: ${e.message}", e)
                            errorMessage = "Ошибка сети: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && isAllFieldsFilled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = saveButtonColor,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2B6B8B),
                    disabledContentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Сохранить", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Ошибка") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                fontSize = 14.sp,
                color = Color(0xFFCBCBCB)
            )
        },
        trailingIcon = trailingIcon,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = TextFieldBackground,
            unfocusedContainerColor = TextFieldBackground,
            focusedBorderColor = if (isError) Color.Red else Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = TextMain,
            focusedTextColor = TextMain,
            unfocusedTextColor = TextMain
        )
    )
}