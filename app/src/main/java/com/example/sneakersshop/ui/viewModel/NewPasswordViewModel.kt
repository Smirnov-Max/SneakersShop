package com.example.sneakersshop.ui.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sneakersshop.data.RetrofitInstance
import com.example.sneakersshop.data.UserSession
import kotlinx.coroutines.launch

class NewPasswordViewModel : ViewModel() {

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun changePassword(newPassword: String, navController: NavController) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null

                val token = UserSession.accessToken
                if (token.isNullOrBlank()) {
                    errorMessage.value = "Ошибка авторизации. Попробуйте снова."
                    return@launch
                }

                val response = RetrofitInstance.userManagementService.changePassword(
                    authHeader = "Bearer $token",
                    body = mapOf("password" to newPassword)
                )

                if (response.isSuccessful) {
                    // После успешной смены пароля переходим на экран входа
                    // Очищаем стек, чтобы нельзя было вернуться назад
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CHANGE_PW", "Ошибка ${response.code()}: $errorBody")
                    errorMessage.value = "Ошибка при смене пароля: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("CHANGE_PW", "Исключение: ${e.message}")
                errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}