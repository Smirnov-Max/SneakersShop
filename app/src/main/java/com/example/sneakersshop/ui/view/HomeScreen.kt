package com.example.sneakersshop.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sneakersshop.R

/**
 * Класс данных, представляющий товар на главном экране
 * @param id уникальный идентификатор товара
 * @param name название товара
 * @param price цена товара в формате строки (с символом рубля)
 * @param imageRes ресурс изображения товара
 */
data class Product(
    val id: Int,
    val name: String,
    val price: String,
    val imageRes: Int
)

/**
 * Главный экран приложения с лентой рекомендаций, категориями и популярными товарами
 *
 * @param navController навигационный контроллер для переходов между экранами
 */
@Composable
fun HomeScreen(navController: NavHostController) {
    // Состояние прокрутки для вертикального скролла
    val scrollState = rememberScrollState()

    // Список доступных категорий для быстрого перехода
    val categories = listOf("Все", "Outdoor", "Tennis")
    // Состояние выбранной категории
    var selectedCategory by remember { mutableStateOf("Все") }

    // Тестовые данные товаров (в реальном приложении будут загружаться из API)
    val products = listOf(
        Product(1, "Nike Air Max", "₽752.00", R.drawable.img_shoe_blue),
        Product(2, "Nike Air Max", "₽752.00", R.drawable.img_shoe_blue)
    )

    // Scaffold - базовая структура экрана с нижней навигацией
    Scaffold(
        bottomBar = { BottomBar(navController = navController, currentRoute = "home") },
        containerColor = Color(0xFFF5F7FA) // Светло-серый фон
    ) { innerPadding ->
        // Основная колонка с прокруткой
        Column(
            modifier = Modifier
                .padding(innerPadding) // Учитываем отступы от Scaffold
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .verticalScroll(scrollState) // Вертикальная прокрутка
                .padding(horizontal = 16.dp, vertical = 12.dp) // Внутренние отступы
        ) {
            // Заголовок экрана
            Text(
                text = stringResource(id = R.string.home_title), // "Hello, David" из ресурсов
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Строка поиска и кнопка фильтра
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Поле поиска (занимает всё доступное пространство)
                SearchBox(
                    hint = stringResource(id = R.string.search_hint), // "Search" из ресурсов
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Кнопка фильтрации
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape) // Круглая форма
                        .background(Color(0xFF48B2E7)) // Голубой фон
                        .clickable { /* TODO: открыть экран фильтрации */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filter",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Заголовок секции категорий
            Text(
                text = stringResource(id = R.string.categories), // "Categories" из ресурсов
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Горизонтальный список категорий
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Отступы между элементами
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    CategoryChip(
                        title = category,
                        selected = isSelected,
                        onClick = {
                            selectedCategory = category
                            // Переход на каталог с выбранной категорией
                            navController.navigate("catalog/$category")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Заголовок секции популярных товаров с ссылкой "See all"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.popular), // "Popular" из ресурсов
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = stringResource(id = R.string.see_all), // "See all" из ресурсов
                    fontSize = 14.sp,
                    color = Color(0xFF48B2E7),
                    modifier = Modifier.clickable {
                        // TODO: переход на полный список популярных товаров
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Горизонтальный список популярных товаров
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    ProductCard(product = product)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Заголовок секции промо-акций
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.promo), // "Promo for you" из ресурсов
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = stringResource(id = R.string.see_all),
                    fontSize = 14.sp,
                    color = Color(0xFF48B2E7)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Промо-баннер
            PromoBanner()

            // Нижний отступ для учета нижней навигации
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Компонент поля поиска с иконкой
 * @param hint текст-подсказка в поле ввода
 * @param modifier модификатор для настройки расположения
 */
@Composable
private fun SearchBox(hint: String, modifier: Modifier = Modifier) {
    var value by remember { mutableStateOf(TextFieldValue("")) } // Состояние текста поиска

    OutlinedTextField(
        value = value,
        onValueChange = { value = it }, // Обновление состояния при вводе
        leadingIcon = {
            // Иконка лупы в начале поля
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color(0xFFB0B0B0) // Серый цвет
            )
        },
        placeholder = {
            Text(text = hint, color = Color(0xFFB0B0B0))
        },
        singleLine = true, // Однострочный ввод
        modifier = modifier
            .height(48.dp) // Фиксированная высота
            .clip(RoundedCornerShape(16.dp)), // Скругленные углы
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent, // Убираем рамку при фокусе
            unfocusedBorderColor = Color.Transparent, // Убираем рамку без фокуса
            focusedContainerColor = Color.White, // Белый фон при фокусе
            unfocusedContainerColor = Color.White // Белый фон без фокуса
        )
    )
}

/**
 * Компонент чипа категории для выбора
 * @param title название категории
 * @param selected флаг выбранной категории
 * @param onClick колбэк при клике на категорию
 */
@Composable
private fun CategoryChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color.White else Color(0xFFE8EDF3)) // Разный фон для выбранной/невыбранной
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = if (selected) Color(0xFF333333) else Color(0xFF828B99) // Разный цвет текста
        )
    }
}

/**
 * Карточка товара для отображения в горизонтальном списке
 * @param product данные товара
 */
@Composable
private fun ProductCard(product: Product) {
    Box(
        modifier = Modifier
            .width(180.dp) // Фиксированная ширина
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp) // Внутренние отступы
    ) {
        Column {
            // Верхний ряд с иконкой избранного
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite_border),
                    contentDescription = "Favorite",
                    tint = Color(0xFFB0B0B0)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Изображение товара
            Image(
                painter = painterResource(id = product.imageRes),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp) // Фиксированная высота
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Бейдж бестселлера (всегда отображается в демо-версии)
            Text(
                text = "BEST SELLER",
                fontSize = 10.sp,
                color = Color(0xFF48B2E7),
                fontWeight = FontWeight.Medium
            )

            // Название товара с обрезкой если не помещается
            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                maxLines = 1, // Максимум одна строка
                overflow = TextOverflow.Ellipsis // Многоточие при обрезке
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Нижний ряд с ценой и кнопкой добавления в корзину
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Цена
                Text(
                    text = product.price,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333)
                )

                // Кнопка добавления в корзину
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF48B2E7))
                        .clickable { /* TODO: добавить в корзину */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cart),
                        contentDescription = "Add to cart",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Промо-баннер для отображения акций
 */
@Composable
private fun PromoBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Фиксированная высота
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
    ) {
        // Фоновое изображение баннера
        Image(
            painter = painterResource(id = R.drawable.img_promo_banner),
            contentDescription = "Promo",
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Компонент нижней навигационной панели
 * Отображает иконки для основных разделов приложения
 *
 * @param navController навигационный контроллер
 * @param currentRoute текущий маршрут для подсветки активной иконки
 */
@Composable
fun BottomBar(navController: NavHostController, currentRoute: String) {
    // Цвета для активной и неактивной иконок
    val activeColor = Color(0xFF48B2E7) // Голубой для активной
    val inactiveColor = Color(0xFFB0B0B0) // Серый для неактивной

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp) // Фиксированная высота панели
            .background(Color.White), // Белый фон
        contentAlignment = Alignment.Center
    ) {
        // Ряд с иконками
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp), // Отступы по бокам
            horizontalArrangement = Arrangement.SpaceBetween, // Равномерное распределение
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка "Домой"
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                tint = if (currentRoute == "home") activeColor else inactiveColor,
                modifier = Modifier.clickable {
                    if (currentRoute != "home") {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false } // Очищаем стек до home
                            launchSingleTop = true // Не создаем дубликатов
                        }
                    }
                }
            )

            // Иконка "Избранное"
            Icon(
                painter = painterResource(id = R.drawable.ic_heart),
                contentDescription = "Favorites",
                tint = if (currentRoute == "favorite") activeColor else inactiveColor,
                modifier = Modifier.clickable {
                    if (currentRoute != "favorite") {
                        navController.navigate("favorite") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )

            // Центральная круглая кнопка для корзины (выделяется)
            Box(
                modifier = Modifier
                    .size(56.dp) // Больше остальных иконок
                    .clip(CircleShape)
                    .background(activeColor), // Всегда голубая
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bag),
                    contentDescription = "Bag",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Иконка "Заказы"
            Icon(
                painter = painterResource(id = R.drawable.ic_truck),
                contentDescription = "Orders",
                tint = inactiveColor // Пока неактивна всегда
            )

            // Иконка "Профиль"
            Icon(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Profile",
                tint = if (currentRoute == "profile") activeColor else inactiveColor,
                modifier = Modifier.clickable {
                    if (currentRoute != "profile") {
                        navController.navigate("profile") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}