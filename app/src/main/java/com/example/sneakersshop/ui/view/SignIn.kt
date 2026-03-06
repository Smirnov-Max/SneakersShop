package com.example.sneakersshop.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.sneakersshop.ui.theme.SneakersShopTheme
import com.example.sneakersshop.ui.viewModel.SignInViewModel
import com.example.sneakersshop.data.model.SignInRequest
import com.example.sneakersshop.R

/**
 * Экран входа в приложение (логин)
 * Позволяет пользователю авторизоваться с помощью email и пароля
 *
 * @param modifier модификатор для настройки компоновки
 * @param navController навигационный контроллер для переходов между экранами
 * @param viewModel view-model для обработки логики входа
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: SignInViewModel = viewModel()
) {
    // Состояния полей ввода
    var email by remember { mutableStateOf("") } // Email пользователя
    var password by remember { mutableStateOf("") } // Пароль
    var showPassword by remember { mutableStateOf(false) } // Отображение/скрытие пароля
    val scrollState = rememberScrollState() // Состояние прокрутки

    // Основная поверхность экрана
    Surface(
        modifier = modifier.fillMaxSize(), // На весь экран
        color = Color.White
    ) {
        // Колонка с прокруткой для всего контента
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // Вертикальная прокрутка
                .padding(horizontal = 24.dp) // Горизонтальные отступы
        ) {
            // Верхний отступ для визуального баланса
            Spacer(modifier = Modifier.height(50.dp))

            // Кнопка "Назад" для возврата к предыдущему экрану
            Box(
                modifier = Modifier
                    .size(32.dp) // Размер 32x32 dp
                    .clip(CircleShape) // Круглая форма
                    .background(Color(0xFFF2F2F2)) // Светло-серый фон
                    .clickable { navController.popBackStack() }, // Возврат на предыдущий экран
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow),
                    contentDescription = "Назад",
                    tint = Color(0xFF555555) // Темно-серый цвет
                )
            }

            // Отступ после кнопки
            Spacer(modifier = Modifier.height(30.dp))

            // Заголовок экрана
            Text(
                text = "Привет!",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Отступ между заголовками
            Spacer(modifier = Modifier.height(4.dp))

            // Подзаголовок с пояснением
            Text(
                text = "Заполните Свои Данные",
                fontSize = 14.sp,
                color = Color(0xFFB0B0B0),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Отступ перед полями ввода
            Spacer(modifier = Modifier.height(40.dp))

            // Поле ввода email
            Text(
                text = "Email",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            StyledTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "xyz@gmail.com",
                keyboardType = KeyboardType.Email // Тип клавиатуры для email
            )

            // Отступ между полями
            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода пароля
            Text(
                text = "Пароль",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            StyledTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "********",
                trailingIcon = {
                    // Иконка для переключения видимости пароля
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword)
                                Icons.Default.VisibilityOff // Глаз перечеркнутый (скрыть)
                            else
                                Icons.Default.Visibility, // Глаз (показать)
                            contentDescription = "Показать/скрыть пароль",
                            tint = Color(0xFFB0B0B0)
                        )
                    }
                },
                keyboardType = KeyboardType.Password, // Тип клавиатуры для пароля
                visualTransformation = if (showPassword)
                    VisualTransformation.None // Без трансформации (видимый текст)
                else
                    PasswordVisualTransformation() // Скрытие символов пароля
            )

            // Отступ после поля пароля
            Spacer(modifier = Modifier.height(12.dp))

            // Ссылка на восстановление пароля
            Text(
                text = "Восстановить",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E),
                modifier = Modifier
                    .align(Alignment.End) // Выравнивание по правому краю
                    .clickable {
                        // Переход на экран восстановления пароля
                        navController.navigate("forgot_password")
                    }
            )

            // Отступ перед кнопкой входа
            Spacer(modifier = Modifier.height(28.dp))

            /**
             * Кнопка входа в приложение
             * Отправляет запрос на авторизацию
             */
            Button(
                onClick = {
                    // Вызов метода входа из ViewModel
                    viewModel.signIn(
                        SignInRequest(email.trim(), password.trim()), // Данные для входа
                        navController = navController
                    )
                },
                modifier = Modifier
                    .fillMaxWidth() // На всю ширину
                    .height(52.dp), // Фиксированная высота
                shape = RoundedCornerShape(18.dp), // Скругленные углы
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF48B2E7), // Голубой цвет кнопки
                    contentColor = Color.White // Белый текст
                )
            ) {
                Text(
                    text = "Войти",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            /**
             * Заполнитель пространства (weight(1f) занимает всё доступное место
             * между кнопкой и нижним текстом, прижимая нижний текст к низу экрана)
             */
            Spacer(modifier = Modifier.weight(1f))

            // Нижняя строка со ссылкой на регистрацию для новых пользователей
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Вы впервые?",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )
                Text(
                    text = " Создать",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.clickable {
                        // Переход на экран регистрации
                        navController.navigate("register")
                    }
                )
            }
        }
    }
}

/**
 * Стилизованное текстовое поле с кастомным оформлением
 * (Переиспользуемый компонент, скопирован для автономности файла)
 *
 * @param value текущее значение поля
 * @param onValueChange колбэк при изменении значения
 * @param placeholder текст-подсказка
 * @param trailingIcon опциональная иконка в конце поля
 * @param keyboardType тип клавиатуры для ввода
 * @param visualTransformation трансформация визуального отображения (для пароля)
 */
@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(), // На всю ширину
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                color = Color(0xFFCBCBCB) // Светло-серый цвет плейсхолдера
            )
        },
        trailingIcon = trailingIcon,
        singleLine = true, // Однострочный ввод
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), // Настройки клавиатуры
        shape = RoundedCornerShape(18.dp), // Скругленные углы
        colors = OutlinedTextFieldDefaults.colors(
            // Цвет текста
            focusedTextColor = Color(0xFF333333), // Темно-серый при фокусе
            unfocusedTextColor = Color(0xFF333333), // Темно-серый без фокуса

            // Цвет фона
            focusedContainerColor = Color(0xFFF7F7F7), // Светло-серый при фокусе
            unfocusedContainerColor = Color(0xFFF7F7F7), // Светло-серый без фокуса

            // Убираем рамку
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,

            // Цвет курсора
            cursorColor = Color(0xFF333333), // Темно-серый

            // Цвет плейсхолдера
            focusedPlaceholderColor = Color(0xFFCBCBCB),
            unfocusedPlaceholderColor = Color(0xFFCBCBCB)
        )
    )
}

/**
 * Предпросмотр экрана входа для разработки
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    SneakersShopTheme {
        val navController = rememberNavController()
        LoginScreen(navController = navController)
    }
}