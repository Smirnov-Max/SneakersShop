package com.example.sneakersshop.ui.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sneakersshop.data.RetrofitInstance
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    val showDialog = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun sendRecoveryEmail(email: String) {
        viewModelScope.launch {
            try {
                errorMessage.value = null
                val response = RetrofitInstance.userManagementService
                    .recoverPassword(mapOf("email" to email))

                if (response.isSuccessful) {
                    showDialog.value = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("FORGOT_PW", "Ошибка ${response.code()}: $errorBody")
                    when (response.code()) {
                        429 -> {
                            errorMessage.value = "Слишком много попыток. Попробуйте позже."
                        }
                        else -> {
                            errorMessage.value = "Ошибка: ${response.code()} ${response.message()}"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FORGOT_PW", "Исключение: ${e.message}")
                errorMessage.value = "Ошибка сети: ${e.message}"
            }
        }
    }
}