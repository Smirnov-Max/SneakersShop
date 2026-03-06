package com.example.sneakersshop.ui.view

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.sneakersshop.ui.theme.SneakersShopTheme
import com.example.sneakersshop.ui.viewModel.SignUpViewModel
import com.example.sneakersshop.R
import com.example.sneakersshop.data.service.ProfileCreationRequest

/**
 * Экран регистрации нового пользователя
 * Содержит форму для ввода имени, email, пароля и согласия на обработку данных
 *
 * @param modifier модификатор для настройки компоновки
 * @param navController навигационный контроллер для переходов между экранами
 * @param viewModel view-model для обработки логики регистрации
 */
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: SignUpViewModel = viewModel()
) {
    // Состояния полей ввода
    var name by remember { mutableStateOf("") } // Имя пользователя
    var email by remember { mutableStateOf("") } // Email
    var password by remember { mutableStateOf("") } // Пароль
    var showPassword by remember { mutableStateOf(false) } // Отображение/скрытие пароля
    var isTermsAccepted by remember { mutableStateOf(false) } // Согласие с условиями

    // Состояние прокрутки для экрана
    val scrollState = rememberScrollState()
    val context = LocalContext.current // Контекст для показа Toast

    /**
     * Валидация формы регистрации
     * Проверяет:
     * - имя не пустое
     * - email соответствует формату
     * - длина пароля не менее 6 символов
     * - согласие с условиями принято
     */
    val isFormValid = name.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            password.length >= 6 &&
            isTermsAccepted

    /**
     * Эффект для отображения ошибок регистрации
     * Срабатывает при изменении errorMessage в ViewModel
     */
    LaunchedEffect(viewModel.errorMessage.value) {
        viewModel.errorMessage.value?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show() // Показываем Toast с ошибкой
        }
    }

    // Основная поверхность экрана
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        // Колонка с прокруткой для всего контента
        Column(
            modifier = Modifier
                .fillMaxSize() // На весь экран
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
                "Регистрация",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Отступ между заголовками
            Spacer(modifier = Modifier.height(4.dp))

            // Подзаголовок с пояснением
            Text(
                "Заполните Свои Данные",
                fontSize = 14.sp,
                color = Color(0xFFB0B0B0),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Отступ перед полями ввода
            Spacer(modifier = Modifier.height(40.dp))

            // Поле ввода имени
            Text(
                "Ваше имя",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            StyledTextField(
                value = name,
                onValueChange = { newValue ->
                    name = newValue.filter { !it.isDigit() }
                },
                placeholder = "Максим Максимов"
            )

            // Отступ между полями
            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода email
            Text(
                "Email",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 6.dp)
            )
            StyledTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "qwerty@gmail.com",
                keyboardType = KeyboardType.Email // Устанавливаем тип клавиатуры для email
            )

            // Отступ между полями
            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода пароля
            Text(
                "Пароль",
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
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFFB0B0B0)
                        )
                    }
                },
                keyboardType = KeyboardType.Password, // Тип клавиатуры для пароля
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation() // Скрытие символов пароля
            )

            // Отступ перед чекбоксом
            Spacer(modifier = Modifier.height(20.dp))

            // Чекбокс согласия на обработку персональных данных
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShieldCheckbox(
                    checked = isTermsAccepted,
                    onCheckedChange = { isTermsAccepted = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Даю согласие на обработку персональных данных",
                    fontSize = 13.sp,
                    color = Color(0xFF4A4A4A)
                )
            }

            // Отступ перед кнопкой
            Spacer(modifier = Modifier.height(28.dp))

            /**
             * Кнопка регистрации
             * Активна только при валидной форме и отсутствии загрузки
             */
            Button(
                onClick = {
                    // Вызов метода регистрации из ViewModel
                    viewModel.signUp(name, email.trim(), password.trim(), navController)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp), // Сильно скругленные углы
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF48B2E7), // Активный цвет (голубой)
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2B6B8B), // Неактивный цвет (темно-синий)
                    disabledContentColor = Color.White
                ),
                enabled = isFormValid && !viewModel.isLoading.value // Кнопка активна только при валидных данных и не в процессе загрузки
            ) {
                if (viewModel.isLoading.value) {
                    // Индикатор загрузки во время выполнения запроса
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Зарегистрироваться",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Строка с ссылкой на вход для существующих пользователей
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Есть аккаунт? ",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )
                Text(
                    "Войти",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    modifier = Modifier.clickable { navController.navigate("login") } // Переход на экран входа
                )
            }
        }
    }
}

/**
 * Стилизованное текстовое поле с кастомным оформлением
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
                placeholder,
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
            focusedContainerColor = Color(0xFFF7F7F7), // Цвет фона при фокусе
            unfocusedContainerColor = Color(0xFFF7F7F7), // Цвет фона без фокуса
            focusedBorderColor = Color.Transparent, // Убираем рамку при фокусе
            unfocusedBorderColor = Color.Transparent // Убираем рамку без фокуса
        )
    )
}

/**
 * Кастомный чекбокс в виде щита
 *
 * @param checked состояние чекбокса (выбран/не выбран)
 * @param onCheckedChange колбэк при изменении состояния
 */
@Composable
fun ShieldCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    // Цвет фона зависит от состояния: голубой если выбран, серый если нет
    val backgroundColor = if (checked) Color(0xFF48B2E7) else Color(0xFFF2F2F2)

    Box(
        modifier = Modifier
            .size(20.dp) // Размер 20x20 dp
            .clip(RoundedCornerShape(4.dp)) // Легкое скругление углов
            .background(backgroundColor)
            .clickable { onCheckedChange(!checked) }, // Переключение состояния при клике
        contentAlignment = Alignment.Center
    ) {
        // Иконка щита внутри чекбокса
        Icon(
            painter = painterResource(id = R.drawable.shield),
            contentDescription = null, // Декоративный элемент
            tint = Color.Black, // Черная иконка
            modifier = Modifier.size(14.dp)
        )
    }
}

/**
 * Предпросмотр экрана регистрации для разработки
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    SneakersShopTheme {
        val navController = rememberNavController()
        RegisterScreen(navController = navController)
    }
}