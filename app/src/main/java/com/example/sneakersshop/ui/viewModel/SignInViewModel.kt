package com.example.sneakersshop.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sneakersshop.data.RetrofitInstance
import com.example.sneakersshop.data.UserSession
import com.example.sneakersshop.data.model.SignInRequest
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана входа в приложение (LoginScreen)
 * Отвечает за аутентификацию пользователя и управление сессией
 */
class SignInViewModel : ViewModel() {

    /**
     * Состояние диалога для отображения сообщений пользователю
     * true - показать диалог
     * false - скрыть диалог
     */
    var showDialog = mutableStateOf(false)

    /**
     * Текст, отображаемый в диалоге (сообщение об ошибке или информация)
     */
    var dialogText = mutableStateOf("")

    /**
     * Выполняет вход пользователя в приложение
     * При успешном входе сохраняет токен и ID пользователя в глобальную сессию
     * При ошибке показывает диалог с сообщением
     *
     * @param signInRequest объект с email и паролем пользователя
     * @param navController навигационный контроллер для перехода на главный экран
     */
    fun signIn(signInRequest: SignInRequest, navController: NavController) {
        // Запускаем корутину в скоупе ViewModel
        viewModelScope.launch {
            try {
                /**
                 * Отправка POST-запроса на сервер для аутентификации
                 * Тело запроса: {"email": "user@example.com", "password": "pass123"}
                 */
                val response = RetrofitInstance.userManagementService.signIn(signInRequest)

                // Проверяем успешность ответа (код 2xx)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        /**
                         * Сохраняем данные авторизации в глобальный объект UserSession
                         * Эти данные будут доступны во всем приложении
                         *
                         * Предполагаемая структура ответа:
                         * {
                         *   "access_token": "eyJhbGciOiJIUzI1NiIs...",
                         *   "user": {
                         *     "id": "123e4567-e89b-12d3-a456-426614174000"
                         *   }
                         * }
                         */
                        val accessToken = body.access_token
                        val userId = body.user.id

                        // Сохраняем токен и ID пользователя в сессию
                        UserSession.accessToken = accessToken
                        UserSession.userId = userId
                    }

                    /**
                     * Переход на главный экран приложения
                     * Очищаем стек навигации, чтобы пользователь не мог вернуться
                     * назад на экран входа через кнопку "Назад"
                     */
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    // Обработка ошибки аутентификации (неверный логин/пароль)
                    dialogText.value = "Неверный логин или пароль"
                    showDialog.value = true
                }
            } catch (e: Exception) {
                // Обработка сетевых ошибок или других исключений
                dialogText.value = "Ошибка: ${e.message}"
                showDialog.value = true
            }
        }
    }
}