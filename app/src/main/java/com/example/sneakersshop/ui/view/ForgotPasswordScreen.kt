package com.example.sneakersshop.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sneakersshop.R
import com.example.sneakersshop.ui.theme.PrimaryBlue
import com.example.sneakersshop.ui.theme.TextFieldBackground
import com.example.sneakersshop.ui.theme.TextMain
import com.example.sneakersshop.ui.theme.TextSecondary
import com.example.sneakersshop.ui.viewModel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val showDialog = viewModel.showDialog.value
    val errorMessage = viewModel.errorMessage.value
    val context = LocalContext.current

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateEmail() {
        emailError = if (email.isNotBlank() && !isEmailValid(email)) {
            "Почта введена некорректно"
        } else null
    }

    val isFormValid = email.isNotBlank() && isEmailValid(email)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDialog.value = false },
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.email_icon),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            title = {
                Text(
                    "Проверьте Ваш Email",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Мы Отправили Код Восстановления Пароля На Вашу Электронную Почту.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showDialog.value = false
                        navController.navigate("verifyOTP/$email/recovery")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("ОК")
                }
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.errorMessage.value = null },
            title = { Text("Ошибка") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.errorMessage.value = null }) {
                    Text("OK")
                }
            }
        )
    }

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

            // Заголовок
            Text(
                text = "Забыл Пароль",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Подзаголовок
            Text(
                text = "Введите Свою Учетную Запись\nДля Сброса",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Метка email
            Text(
                text = "Email",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMain,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Поле ввода email
            StyledTextField(
                value = email,
                onValueChange = {
                    email = it
                    validateEmail()
                },
                placeholder = "xyz@gmail.com",
                keyboardType = KeyboardType.Email,
                isError = emailError != null
            )

            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Кнопка отправки
            Button(
                onClick = {
                    if (isFormValid) {
                        viewModel.sendRecoveryEmail(email)
                    } else {
                        validateEmail()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2B6B8B),
                    disabledContentColor = Color.White
                )
            ) {
                Text("Отправить", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
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