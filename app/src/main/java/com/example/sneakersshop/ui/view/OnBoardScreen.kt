package com.example.sneakersshop.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.example.sneakersshop.R

/**
 * Экран онбординга (приветственные слайды), который показывается при первом запуске приложения
 * Содержит 3 слайда с возможностью пролистывания и кнопкой для перехода к регистрации
 *
 * @param navController навигационный контроллер для перехода на экран регистрации
 */
@OptIn(ExperimentalFoundationApi::class) // Аннотация для использования экспериментального HorizontalPager
@Composable
fun OnboardingScreen(navController: NavHostController) {
    // Состояние пагера (свайпера) с 3 страницами
    val pagerState = rememberPagerState(pageCount = { 3 })
    // Корутин скоуп для анимации переключения страниц
    val coroutineScope = rememberCoroutineScope()

    /**
     * Данные для отображения на каждом слайде
     * Каждый слайд содержит изображение, заголовок и описание
     */
    val pages = listOf(
        OnboardPageData(
            imageRes = R.drawable.onboard1, // Первое изображение
            title = "ДОБРО\nПОЖАЛОВАТЬ",    // Заголовок с переносом строки
            subtitle = "",                   // Пустой подзаголовок для первого слайда
            isFirstPage = true                // Флаг первого слайда для специального позиционирования
        ),
        OnboardPageData(
            imageRes = R.drawable.onboard2,
            title = "Начнем\nпутешествие",
            subtitle = "Умная, великолепная и модная\nколлекция. Изучите сейчас",
            isFirstPage = false
        ),
        OnboardPageData(
            imageRes = R.drawable.onboard3,
            title = "У Вас Есть Сила,\nЧтобы",
            subtitle = "В вашей комнате много красивых\nи привлекательных растений",
            isFirstPage = false
        )
    )

    // Основной контейнер с градиентным фоном
    Box(
        modifier = Modifier
            .fillMaxSize() // На весь экран
            .background(
                // Вертикальный градиент от голубого к темно-синему
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF48B2E7), // Светло-голубой
                        Color(0xFF44A9DC), // Голубой
                        Color(0xFF2B6B8B)  // Темно-синий
                    )
                )
            )
    ) {
        // Основная колонка с контентом
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp), // Отступы по краям
            verticalArrangement = Arrangement.SpaceBetween // Распределяем пространство между элементами
        ) {
            // Верхний отступ для баланса
            Spacer(modifier = Modifier.height(16.dp))

            /**
             * HorizontalPager - компонент для создания свайпаемых страниц
             * Позволяет перелистывать слайды горизонтальным свайпом
             */
            HorizontalPager(
                state = pagerState, // Состояние пагера
                modifier = Modifier.weight(1f) // Занимает всё доступное пространство
            ) { page ->
                // Отображение текущей страницы
                OnboardPage(
                    data = pages[page]
                )
            }

            // Блок с индикаторами и кнопкой (внизу экрана)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                /**
                 * Индикаторы текущей страницы (точечки)
                 * Показывают, на каком слайде пользователь находится
                 */
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    // Создаем индикатор для каждой страницы
                    repeat(pagerState.pageCount) { index ->
                        val isSelected = pagerState.currentPage == index // Активна ли текущая страница
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp) // Отступы между точками
                                .width(if (isSelected) 24.dp else 8.dp) // Активная длиннее
                                .height(6.dp) // Фиксированная высота
                                .background(
                                    color = if (isSelected) Color.White else Color(0x55FFFFFF), // Активная ярче
                                    shape = RoundedCornerShape(3.dp) // Скругленные углы
                                )
                        )
                    }
                }

                /**
                 * Кнопка действия
                 * На первых двух слайдах - "Далее" (переход к следующему)
                 * На последнем слайде - "Завершить" (переход к регистрации)
                 */
                Button(
                    onClick = {
                        // Запускаем анимацию переключения страниц
                        coroutineScope.launch {
                            if (pagerState.currentPage < pagerState.pageCount - 1) {
                                // Если не последняя страница - листаем дальше
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                // Если последняя страница - переходим к регистрации
                                navController.navigate("register")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth() // На всю ширину
                        .height(52.dp), // Фиксированная высота
                    shape = RoundedCornerShape(20.dp), // Сильно скругленные углы
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, // Белая кнопка
                        contentColor = Color(0xFF48B2E7) // Голубой текст
                    )
                ) {
                    // Текст меняется в зависимости от текущей страницы
                    Text(
                        text = if (pagerState.currentPage == pagerState.pageCount - 1) "Завершить" else "Далее",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Класс данных для хранения информации о слайде онбординга
 * @param imageRes ресурс изображения
 * @param title заголовок слайда
 * @param subtitle подзаголовок/описание (может быть пустым)
 * @param isFirstPage флаг первого слайда для специального позиционирования изображения
 */
data class OnboardPageData(
    val imageRes: Int,
    val title: String,
    val subtitle: String,
    val isFirstPage: Boolean
)

/**
 * Компонент отдельной страницы онбординга
 * Отображает изображение, заголовок и описание
 *
 * @param data данные для отображения на странице
 */
@Composable
fun OnboardPage(
    data: OnboardPageData
) {
    // Колонка с контентом страницы
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp), // Без горизонтальных отступов для изображения
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (data.isFirstPage) {
            /**
             * Специальное позиционирование для первого слайда
             * Изображение смещено вправо и вверх для лучшей композиции
             */
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = null, // Декоративное изображение, описание не требуется
                modifier = Modifier
                    .fillMaxWidth(1f) // На всю ширину
                    .height(320.dp) // Фиксированная высота
                    .offset(x = 20.dp, y = (-20).dp) // Смещение вправо и вверх
            )
        } else {
            /**
             * Стандартное позиционирование для остальных слайдов
             * Изображение смещено только вверх
             */
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height(340.dp) // Чуть выше, чем на первом слайде
                    .offset(y = (-40).dp) // Смещение вверх
            )
        }

        // Отступ после изображения (разный для первого и остальных слайдов)
        Spacer(modifier = Modifier.height(if (data.isFirstPage) 24.dp else 16.dp))

        // Заголовок
        Text(
            text = data.title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        // Подзаголовок (отображается только если не пустой)
        if (data.subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = data.subtitle,
                fontSize = 14.sp,
                color = Color(0xFFE0E0E0), // Светло-серый
                textAlign = TextAlign.Center
            )
        }
    }
}