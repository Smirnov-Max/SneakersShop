package com.example.sneakersshop.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.sneakersshop.R
import com.example.sneakersshop.ui.theme.PrimaryBlue
import com.example.sneakersshop.ui.theme.TextFieldBackground
import com.example.sneakersshop.ui.theme.TextMain
import com.example.sneakersshop.ui.theme.TextSecondary
import com.example.sneakersshop.ui.viewModel.NewPasswordViewModel

@Composable
fun NewPasswordScreen(
    navController: NavHostController,
    email: String, // больше не используется, но оставлен для совместимости
    viewModel: NewPasswordViewModel = viewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val passwordError = when {
        password.isBlank() || confirmPassword.isBlank() -> null
        password.length < 6 -> "Пароль должен быть минимум 6 символов"
        password != confirmPassword -> "Пароли не совпадают"
        else -> null
    }

    val isValid = password.length >= 6 && password == confirmPassword

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                text = "Задать Новый Пароль",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Установите Новый Пароль Для Входа В\nВашу Учетную Запись",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Поле "Пароль"
            Text(
                text = "Пароль",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMain,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            StyledTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "********",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле "Подтверждение пароля"
            Text(
                text = "Подтверждение пароля",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMain,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            StyledTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "********",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null
            )

            if (passwordError != null) {
                Text(
                    text = passwordError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Кнопка "Сохранить"
            Button(
                onClick = {
                    if (isValid) {
                        viewModel.changePassword(password, navController)
                    }
                },
                enabled = isValid && !viewModel.isLoading.value,
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
                if (viewModel.isLoading.value) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Сохранить", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
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