package com.example.sneakersshop.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sneakersshop.data.RetrofitInstance
import com.example.sneakersshop.data.UserSession
import kotlinx.coroutines.launch
import com.example.sneakersshop.R

/**
 * Экран избранного - отображает товары, добавленные пользователем в избранное
 * Позволяет просматривать и удалять товары из избранного
 *
 * @param navController навигационный контроллер для переходов между экранами
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(navController: NavHostController) {
    // Получение данных авторизации текущего пользователя
    val token = UserSession.accessToken
    val userId = UserSession.userId
    // Создание корутин скоупа для асинхронных операций
    val scope = rememberCoroutineScope()

    // Состояния экрана
    var products by remember { mutableStateOf<List<CatalogProduct>>(emptyList()) } // Список избранных товаров
    var isLoading by remember { mutableStateOf(false) } // Флаг загрузки данных

    /**
     * Загрузка списка избранных товаров при первом входе на экран
     * Срабатывает при изменении token или userId
     */
    LaunchedEffect(token, userId) {
        // Проверка авторизации
        if (token == null || userId == null) return@LaunchedEffect
        isLoading = true
        try {
            val service = RetrofitInstance.userManagementService

            // Загрузка ID избранных товаров пользователя
            val favs = service.getFavourites(
                authHeader = "Bearer $token",
                userIdFilter = "eq.$userId", // Фильтр по ID пользователя
                select = "product_id" // Запрашиваем только ID товаров для оптимизации
            )

            // Если избранное пустое, очищаем список
            if (favs.isEmpty()) {
                products = emptyList()
            } else {
                // Загружаем все товары для получения полной информации
                val allProducts = service.getProducts(
                    authHeader = "Bearer $token"
                )
                // Создаем множество ID избранных товаров
                val favIds = favs.mapNotNull { it.product_id }.toSet()
                // Фильтруем товары, оставляя только избранные
                products = allProducts
                    .filter { favIds.contains(it.id) }
                    .map { p ->
                        CatalogProduct(
                            id = p.id,
                            title = p.title,
                            price = p.cost,
                            categoryId = p.category_id,
                            isBestSeller = p.is_best_seller == true,
                            imageRes = R.drawable.img_shoe_blue, // Изображение-заглушка
                            isFavorite = true // Все товары на этом экране по умолчанию в избранном
                        )
                    }
            }
        } finally {
            isLoading = false
        }
    }

    /**
     * Функция удаления товара из избранного
     * Выполняется прямо на экране избранного без перезагрузки
     *
     * @param product товар, который нужно удалить из избранного
     */
    fun removeFromFavourite(product: CatalogProduct) {
        // Проверка авторизации
        if (token == null || userId == null) return
        scope.launch {
            try {
                val service = RetrofitInstance.userManagementService
                // Отправка запроса на удаление из избранного
                service.deleteFavourite(
                    authHeader = "Bearer $token",
                    userIdFilter = "eq.$userId",
                    productIdFilter = "eq.${product.id}" // Фильтр по ID товара
                )
                // Обновление локального состояния - удаляем товар из списка
                products = products.filter { it.id != product.id }
            } catch (_: Exception) {
                // Игнорируем ошибки для улучшения UX
                // Можно добавить логирование при необходимости
            }
        }
    }

    // Scaffold - базовая структура экрана с нижней навигацией
    Scaffold(
        bottomBar = { BottomBar(navController = navController, currentRoute = "favorite") },
        containerColor = Color(0xFFF5F7FB) // Фоновый цвет
    ) { innerPadding ->
        /**
         * Корректировка отступов от Scaffold
         * Убираем верхний отступ, оставляем только снизу для правильного расположения контента
         */
        val contentPadding = PaddingValues(
            start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
            end = innerPadding.calculateRightPadding(LayoutDirection.Ltr),
            bottom = innerPadding.calculateBottomPadding()
        )

        // Основной контент экрана
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
        ) {
            // Верхняя панель с заголовком и навигацией
            TopAppBar(
                title = {
                    Text(
                        text = "Избранное",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    // Кнопка назад
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    // Иконка-индикатор избранного в правой части панели
                    Icon(
                        painter = painterResource(id = R.drawable.ic_heart_filled),
                        contentDescription = "Favorite",
                        tint = Color(0xFFDD4B4B), // Красный цвет
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(20.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F7FB) // Прозрачный фон
                )
            )

            // Отображение индикатора загрузки или сетки товаров
            if (isLoading) {
                // Центрированный индикатор загрузки
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF48B2E7))
                }
            } else {
                // Сетка избранных товаров в 2 колонки
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Фиксированное количество колонок
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp), // Отступ снизу
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // Отступ между колонками
                    verticalArrangement = Arrangement.spacedBy(12.dp) // Отступ между рядами
                ) {
                    // Отображение каждого товара в виде карточки
                    items(products, key = { it.id }) { product ->
                        FavoriteProductCard(
                            product = product,
                            onRemove = { removeFromFavourite(product) } // Передаем функцию удаления
                        )
                    }
                }
            }
        }
    }
}

/**
 * Карточка избранного товара для отображения в сетке
 * Визуально похожа на карточку в каталоге, но с особым поведением для удаления из избранного
 *
 * @param product данные товара для отображения
 * @param onRemove колбэк, вызываемый при нажатии на сердечко для удаления из избранного
 */
@Composable
private fun FavoriteProductCard(
    product: CatalogProduct,
    onRemove: () -> Unit
) {
    // Карточка товара
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp)) // Скругление углов
            .background(Color.White) // Белый фон
            .padding(10.dp) // Внутренние отступы
    ) {
        Column {
            // Верхний ряд с иконкой избранного
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Выравнивание по правому краю
            ) {
                // Контейнер для иконки сердечка
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White), // Белый фон для контраста
                    contentAlignment = Alignment.Center
                ) {
                    // Иконка заполненного сердца (всегда красная на этом экране)
                    Icon(
                        painter = painterResource(id = R.drawable.ic_heart_filled),
                        contentDescription = "Favorite",
                        tint = Color(0xFFDD4B4B), // Красный цвет
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onRemove() } // При клике удаляем из избранного
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Контейнер для изображения товара
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF2F4F7)), // Светло-серый фон
                contentAlignment = Alignment.Center
            ) {
                // Изображение товара
                Image(
                    painter = painterResource(id = product.imageRes),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit // Масштабирование с сохранением пропорций
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Отображение бейджа "BEST SELLER" если товар бестселлер
            if (product.isBestSeller) {
                Text(
                    text = "BEST SELLER",
                    fontSize = 10.sp,
                    color = Color(0xFF48B2E7), // Голубой цвет
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Название товара
            Text(
                text = product.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Нижний ряд с ценой и кнопкой добавления в корзину
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Цена товара
                Text(
                    text = "₽${product.price}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f) // Занимает все доступное пространство слева
                )

                // Круглая кнопка "+" для добавления в корзину
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape) // Круглая форма
                        .background(Color(0xFF48B2E7)), // Голубой фон
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}