package com.example.sneakersshop.ui.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sneakersshop.data.RetrofitInstance
import com.example.sneakersshop.data.model.SignUpRequest
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun signUp(name: String, email: String, password: String, navController: NavController) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null

                val response = RetrofitInstance.userManagementService
                    .signUp(
                        SignUpRequest(
                            email = email,
                            password = password,
                            data = mapOf("name" to name)   // ← передаём имя
                        )
                    )

                if (response.isSuccessful) {
                    navController.navigate("verifyOTP/$email/signup")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SIGN_UP", "Ошибка ${response.code()}: $errorBody")
                    errorMessage.value = "Ошибка регистрации: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("SIGN_UP", "Исключение: ${e.message}")
                errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}