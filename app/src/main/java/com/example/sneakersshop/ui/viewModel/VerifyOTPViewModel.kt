package com.example.sneakersshop.ui.viewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sneakersshop.data.RetrofitInstance
import com.example.sneakersshop.data.UserSession
import com.example.sneakersshop.data.model.VerifyOtpRequest
import com.example.sneakersshop.data.model.VerifyOtpResponse
import kotlinx.coroutines.launch

class VerifyOTPViewModel : ViewModel() {

    fun verifyOTP(
        email: String,
        token: String,
        type: String,
        context: Context,
        navController: NavController
    ) {
        viewModelScope.launch {
            try {
                val requestType = if (type == "recovery") "recovery" else "signup"
                val request = VerifyOtpRequest(
                    type = requestType,
                    email = email,
                    token = token
                )

                val response: retrofit2.Response<VerifyOtpResponse> = RetrofitInstance.userManagementService.verifyOTP(request)

                if (response.isSuccessful) {
                    val body: VerifyOtpResponse? = response.body()
                    if (type == "recovery") {
                        // Сохраняем токен, полученный при верификации
                        body?.access_token?.let { UserSession.accessToken = it }
                        navController.navigate("new_password/$email")
                    } else {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("OTP_ERROR", "Код ответа: ${response.code()}, тело: $errorBody")
                    Toast.makeText(context, "Неверный код (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("OTP_ERROR", "Исключение при проверке OTP: ${e.message}")
                Toast.makeText(context, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resendOtp(
        email: String,
        type: String,
        context: Context,
        onRateLimit: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val requestType = if (type == "recovery") "recovery" else "signup"
                val response = RetrofitInstance.userManagementService.resendOTP(
                    mapOf(
                        "type" to requestType,
                        "email" to email
                    )
                )
                if (response.isSuccessful) {
                    Toast.makeText(context, "Код отправлен повторно", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RESEND_ERROR", "Код ответа: ${response.code()}, тело: $errorBody")
                    when (response.code()) {
                        429 -> {
                            onRateLimit()
                            Toast.makeText(context, "Слишком много попыток. Попробуйте через минуту.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(context, "Ошибка при повторной отправке (${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RESEND_ERROR", "Исключение: ${e.message}")
                Toast.makeText(context, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}