package com.example.sneakersshop.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.sneakersshop.R
import com.example.sneakersshop.ui.theme.*
import com.example.sneakersshop.ui.viewModel.VerifyOTPViewModel
import kotlinx.coroutines.delay

@Composable
fun VerifyOTPScreen(
    navController: NavHostController,
    email: String,
    otpType: String = "signup",
    viewModel: VerifyOTPViewModel = viewModel()
) {
    var otpValue by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current
    val otpLength = 6

    var timerSeconds by remember { mutableStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timerSeconds > 0) {
                delay(1000L)
                timerSeconds--
            }
            isTimerRunning = false
            canResend = true
        }
    }

    LaunchedEffect(otpValue.text) {
        if (otpValue.text.length == otpLength) {
            viewModel.verifyOTP(email, otpValue.text, otpType, context, navController)
        }
    }

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

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "OTP Проверка",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Пожалуйста, Проверьте Свою\nЭлектронную Почту, Чтобы Увидеть Код\nПодтверждения",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "OTP Код",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OtpInputField(
                otpValue = otpValue,
                onValueChange = {
                    if (it.text.length <= otpLength) {
                        otpValue = it
                    }
                },
                length = otpLength
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Строка с кнопкой повторной отправки и таймером
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Отправить заново",
                    fontSize = 12.sp,
                    color = if (canResend) PrimaryBlue else TextSecondary,
                    modifier = Modifier
                        .clickable(enabled = canResend) {
                            if (canResend) {
                                viewModel.resendOtp(
                                    email = email,
                                    type = otpType,
                                    context = context,
                                    onRateLimit = {
                                        timerSeconds = 60
                                        isTimerRunning = true
                                        canResend = false
                                    }
                                )
                                timerSeconds = 60
                                isTimerRunning = true
                                canResend = false
                            }
                        }
                )

                Text(
                    text = "00:${timerSeconds.toString().padStart(2, '0')}",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun OtpInputField(
    otpValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    length: Int
) {
    Box(contentAlignment = Alignment.CenterStart) {
        BasicTextField(
            value = otpValue,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(length) { index ->
                        val char = if (index < otpValue.text.length) otpValue.text[index] else null
                        val isFocused = index == otpValue.text.length
                        OtpCell(char = char, isFocused = isFocused)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent)
        )
    }
}

@Composable
fun OtpCell(char: Char?, isFocused: Boolean) {
    val borderColor = if (isFocused) OtpFocusedBorder else TextFieldBackground
    val backgroundColor = TextFieldBackground

    Box(
        modifier = Modifier
            .width(42.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char?.toString() ?: "",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VerifyOTPScreenPreview() {
    SneakersShopTheme {
        val navController = rememberNavController()
        VerifyOTPScreen(
            navController = navController,
            email = "test@example.com",
            otpType = "recovery"
        )
    }
}