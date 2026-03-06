package com.example.sneakersshop.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sneakersshop.data.RetrofitInstance
import com.example.sneakersshop.data.UserSession
import com.example.sneakersshop.data.model.FavouriteRequest
import com.example.sneakersshop.data.service.ProductDto
import kotlinx.coroutines.launch
import com.example.sneakersshop.R

/**
 * Экран детальной информации о товаре
 * Отображает подробное описание, изображение, цену и позволяет управлять избранным
 *
 * @param navController навигационный контроллер для переходов между экранами
 * @param productId идентификатор товара, переданный через навигацию
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    navController: NavHostController,
    productId: String
) {
    // Получение данных авторизации текущего пользователя из глобальной сессии
    val token = UserSession.accessToken
    val userId = UserSession.userId
    // Создание корутин скоупа для выполнения асинхронных операций
    val scope = rememberCoroutineScope()

    // Состояния экрана
    var allProducts by remember { mutableStateOf<List<CatalogProduct>>(emptyList()) } // Все доступные товары
    var current by remember { mutableStateOf<CatalogProduct?>(null) } // Текущий отображаемый товар
    var isLoading by remember { mutableStateOf(false) } // Флаг загрузки данных

    /**
     * Эффект загрузки данных при первом входе на экран
     * Срабатывает при изменении productId, token или userId
     * Загружает все товары и определяет текущий по productId
     */
    LaunchedEffect(productId, token, userId) {
        // Проверка авторизации
        if (token == null || userId == null) return@LaunchedEffect
        isLoading = true
        try {
            val service = RetrofitInstance.userManagementService

            // Загрузка всех товаров
            val products: List<ProductDto> = service.getProducts(
                authHeader = "Bearer $token"
            )

            // Загрузка списка избранного текущего пользователя
            val favs = service.getFavourites(
                authHeader = "Bearer $token",
                userIdFilter = "eq.$userId"
            )

            // Создание множества ID товаров в избранном
            val favSet = favs.mapNotNull { it.product_id }.toSet()

            // Преобразование DTO в модель UI
            val mapped = products.map { p ->
                CatalogProduct(
                    id = p.id,
                    title = p.title,
                    price = p.cost,
                    categoryId = p.category_id,
                    isBestSeller = p.is_best_seller == true,
                    imageRes = R.drawable.img_shoe_blue, // Изображение-заглушка
                    isFavorite = favSet.contains(p.id),
                    description = p.description
                )
            }
            allProducts = mapped
            // Поиск текущего товара по ID, если не найден - берем первый
            current = mapped.firstOrNull { it.id == productId } ?: mapped.firstOrNull()
        } finally {
            isLoading = false
        }
    }

    /**
     * Функция переключения статуса избранного для товара
     * Отправляет запрос на сервер и обновляет локальное состояние
     *
     * @param product товар, для которого меняется статус
     * @param isFav новый статус избранного (true - добавить, false - удалить)
     */
    fun toggleFavourite(product: CatalogProduct, isFav: Boolean) {
        // Проверка авторизации
        if (token == null || userId == null) return
        scope.launch {
            try {
                val service = RetrofitInstance.userManagementService
                if (isFav) {
                    // Добавление в избранное
                    service.addFavourite(
                        authHeader = "Bearer $token",
                        body = FavouriteRequest(
                            user_id = userId,
                            product_id = product.id
                        )
                    )
                } else {
                    // Удаление из избранного
                    service.deleteFavourite(
                        authHeader = "Bearer $token",
                        userIdFilter = "eq.$userId",
                        productIdFilter = "eq.${product.id}"
                    )
                }
                // Обновление состояния всех товаров
                allProducts = allProducts.map {
                    if (it.id == product.id) it.copy(isFavorite = isFav) else it
                }
                // Обновление состояния текущего товара
                current = current?.let {
                    if (it.id == product.id) it.copy(isFavorite = isFav) else it
                }
            } catch (_: Exception) {
                // Игнорируем ошибки сети для улучшения UX
            }
        }
    }

    val product = current

    // Scaffold - базовая структура экрана с поддержкой внутренних отступов
    Scaffold(
        containerColor = Color(0xFFF5F7FB) // Фоновый цвет всего экрана
    ) { innerPadding ->
        // Отображение индикатора загрузки или контента
        if (isLoading || product == null) {
            // Центрированный индикатор загрузки
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF48B2E7))
            }
        } else {
            // Основной контент экрана
            Column(
                modifier = Modifier
                    // Применяем отступы от Scaffold правильно с учетом направления
                    .padding(
                        start = innerPadding.calculateLeftPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateRightPadding(LocalLayoutDirection.current),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FB))
            ) {
                // Верхняя панель с навигацией и заголовком
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Кнопка назад
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = "Назад"
                        )
                    }
                    // Заголовок магазина
                    Text(
                        text = "Sneaker Shop",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Кнопка избранного для текущего товара
                    IconButton(
                        onClick = { toggleFavourite(product, !product.isFavorite) }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (product.isFavorite)
                                    R.drawable.ic_heart_filled // Заполненное сердце
                                else
                                    R.drawable.ic_favorite_border // Пустое сердце
                            ),
                            contentDescription = "Favorite",
                            tint = if (product.isFavorite) Color(0xFFDD4B4B) else Color(0xFFB0B0B0)
                        )
                    }
                }

                // Основная информация о товаре (с прокруткой)
                Column(
                    modifier = Modifier
                        .weight(1f) // Занимает все доступное пространство между верхней и нижней панелями
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Название товара
                    Text(
                        text = product.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222222)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Категория товара (захардкожено для демонстрации)
                    Text(
                        text = "Men's Shoes",
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Цена товара
                    Text(
                        text = "₽${product.price}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF222222)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Контейнер для основного изображения товара
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = product.imageRes),
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit // Масштабирование с сохранением пропорций
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    /**
                     * Горизонтальная галерея миниатюр всех товаров
                     * Позволяет быстро переключаться между товарами
                     */
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()), // Горизонтальная прокрутка
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        allProducts.forEach { p ->
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (p.id == product.id)
                                            Color(0xFFE2F3FF) // Подсветка выбранного товара
                                        else
                                            Color(0xFFF2F4F7) // Обычный фон для остальных
                                    )
                                    .clickable {
                                        current = p // Переключение на выбранный товар
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = p.imageRes),
                                    contentDescription = p.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Заголовок секции описания
                    Text(
                        text = "Описание",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF222222)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Текст описания товара из базы данных
                    Text(
                        text = product.description,
                        fontSize = 13.sp,
                        color = Color(0xFF555555)
                    )
                }

                // Нижняя панель с кнопками действий
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Кнопка избранного (круглая)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                toggleFavourite(product, !product.isFavorite)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (product.isFavorite)
                                    R.drawable.ic_heart_filled
                                else
                                    R.drawable.ic_favorite_border
                            ),
                            contentDescription = "Favorite",
                            tint = if (product.isFavorite) Color(0xFFDD4B4B) else Color(0xFFB0B0B0)
                        )
                    }

                    // Кнопка добавления в корзину
                    Button(
                        onClick = { /* TODO: добавить в корзину */ },
                        modifier = Modifier
                            .weight(1f) // Занимает оставшееся пространство
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF48B2E7)
                        )
                    ) {
                        // Иконка корзины
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cart),
                            contentDescription = null, // Декоративный элемент
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Текст кнопки
                        Text(
                            text = "В корзину",
                            fontSize = 15.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}