package com.example.sneakersshop.ui.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
 * Класс данных, представляющий категорию товаров в каталоге
 * @param id уникальный идентификатор категории в базе данных
 * @param title отображаемое название категории для пользователя
 */
data class CatalogCategory(
    val id: String,
    val title: String
)

/**
 * Класс данных, представляющий товар в каталоге
 * @param id уникальный идентификатор товара
 * @param title название товара
 * @param price цена товара
 * @param categoryId идентификатор категории, к которой относится товар
 * @param isBestSeller флаг, указывающий является ли товар бестселлером
 * @param imageRes ресурс изображения товара
 * @param isFavorite флаг, находится ли товар в избранном у текущего пользователя
 * @param description подробное описание товара из базы данных
 */
data class CatalogProduct(
    val id: String,
    val title: String,
    val price: Double,
    val categoryId: String?,
    val isBestSeller: Boolean,
    val imageRes: Int,
    val isFavorite: Boolean = false,
    val description: String = ""        // описание из базы
)

/**
 * Главный экран каталога товаров
 * @param navController навигационный контроллер для переходов между экранами
 * @param initialCategoryTitle начальная выбранная категория (по умолчанию "Outdoor")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    navController: NavHostController,
    initialCategoryTitle: String = "Outdoor"
) {
    // Предопределенный список категорий с их ID из базы данных
    val categories = listOf(
        CatalogCategory("all", "Все"),
        CatalogCategory("ea4ed603-8cbe-4d57-a359-b6b843a645bc", "Outdoor"),
        CatalogCategory("4f3a690b-41bf-4fca-8ffc-67cc385c6637", "Tennis"),
        CatalogCategory("76ab9d74-7d5b-4dee-9c67-6ed4019fa202", "Men"),
        CatalogCategory("8143b506-d70a-41ec-a5eb-3cf09627da9e", "Women")
    )

    // Получение данных текущего пользователя из глобального объекта сессии
    val sessionUserId = UserSession.userId
    val token = UserSession.accessToken
    // Создание корутин скоупа для выполнения асинхронных операций
    val scope = rememberCoroutineScope()

    // Состояния экрана
    var allProducts by remember { mutableStateOf<List<CatalogProduct>>(emptyList()) } // Список всех товаров
    var selectedCategory by remember { mutableStateOf(initialCategoryTitle) } // Выбранная категория
    var isLoading by remember { mutableStateOf(false) } // Флаг загрузки данных

    // Логирование для отладки - проверяем наличие данных пользователя
    Log.d("CATALOG", "sessionUserId=$sessionUserId token=${token?.take(10)}")

    /**
     * Эффект для загрузки данных при первом входе на экран
     * Срабатывает при изменении sessionUserId или token
     */
    LaunchedEffect(sessionUserId, token) {
        // Проверка авторизации пользователя
        if (token == null || sessionUserId == null) {
            Log.e("CATALOG", "No token or userId, skip loading")
            return@LaunchedEffect
        }
        isLoading = true
        try {
            val service = RetrofitInstance.userManagementService

            // Загрузка всех товаров с сервера
            val products: List<ProductDto> = service.getProducts(
                authHeader = "Bearer $token"
            )

            // Загрузка списка избранных товаров текущего пользователя
            val favs = service.getFavourites(
                authHeader = "Bearer $token",
                userIdFilter = "eq.$sessionUserId" // Фильтр по ID пользователя
            )

            // Создание множества ID товаров, которые находятся в избранном
            val favSet = favs.mapNotNull { it.product_id }.toSet()

            // Преобразование DTO в модель UI и объединение с данными об избранном
            allProducts = products.map { p ->
                CatalogProduct(
                    id = p.id,
                    title = p.title,
                    price = p.cost,
                    categoryId = p.category_id,
                    isBestSeller = p.is_best_seller == true,
                    imageRes = R.drawable.img_shoe_blue, // Временное изображение-заглушка
                    isFavorite = favSet.contains(p.id), // Проверка, есть ли товар в избранном
                    description = p.description // Описание из базы данных
                )
            }
        } catch (e: Exception) {
            Log.e("CATALOG", "load error", e)
        } finally {
            isLoading = false
        }
    }

    /**
     * Функция переключения статуса избранного для товара
     * Отправляет запрос на сервер для добавления или удаления из избранного
     * @param product товар, для которого меняется статус
     * @param isFav новый статус избранного (true - добавить, false - удалить)
     */
    fun toggleFavourite(product: CatalogProduct, isFav: Boolean) {
        // Проверка наличия данных авторизации
        if (sessionUserId == null || token == null) {
            Log.e("FAV", "No token/userId")
            return
        }

        // Запуск корутины для выполнения сетевого запроса
        scope.launch {
            try {
                val service = RetrofitInstance.userManagementService

                if (isFav) {
                    // Отправка запроса на добавление в избранное
                    val resp = service.addFavourite(
                        authHeader = "Bearer $token",
                        body = FavouriteRequest(
                            user_id = sessionUserId,
                            product_id = product.id
                        )
                    )
                    Log.d("FAV", "addFavourite code=${resp.code()} err=${resp.errorBody()?.string()}")

                    // Если запрос не успешен, откатываем изменение статуса
                    if (!resp.isSuccessful) {
                        allProducts = allProducts.map {
                            if (it.id == product.id) it.copy(isFavorite = false) else it
                        }
                        return@launch
                    }
                } else {
                    // Отправка запроса на удаление из избранного
                    val resp = service.deleteFavourite(
                        authHeader = "Bearer $token",
                        userIdFilter = "eq.$sessionUserId",
                        productIdFilter = "eq.${product.id}"
                    )
                    Log.d("FAV", "deleteFavourite code=${resp.code()} err=${resp.errorBody()?.string()}")
                }

                // Обновление локального состояния после успешного запроса
                allProducts = allProducts.map {
                    if (it.id == product.id) it.copy(isFavorite = isFav) else it
                }
            } catch (e: Exception) {
                Log.e("FAV", "toggle error", e)
                // При ошибке откатываем изменение статуса
                allProducts = allProducts.map {
                    if (it.id == product.id) it.copy(isFavorite = !isFav) else it
                }
            }
        }
    }

    // Определение текущей выбранной категории по названию
    val currentCategory = categories.find { it.title == selectedCategory }

    // Фильтрация товаров по выбранной категории
    val filteredProducts = allProducts.filter { product ->
        when (currentCategory?.id) {
            null, "all" -> true // Если категория не выбрана или выбрана "Все", показываем все товары
            else -> product.categoryId == currentCategory.id // Иначе фильтруем по ID категории
        }
    }

    // Основная верстка экрана
    Column(
        modifier = Modifier
            .fillMaxSize() // Занимает весь доступный размер
            .background(Color(0xFFF5F7FB)) // Фоновый цвет всего экрана
    ) {
        // Верхняя панель с заголовком и кнопкой назад
        TopAppBar(
            title = {
                Text(
                    text = selectedCategory, // Отображение названия выбранной категории
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                // Кнопка возврата на предыдущий экран
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow),
                        contentDescription = "Назад"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF5F7FB) // Цвет фона панели
            )
        )

        // Блок с категориями для горизонтальной прокрутки
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Заголовок секции категорий
            Text(
                text = "Категории",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Горизонтальный список категорий с возможностью прокрутки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()), // Горизонтальная прокрутка
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Отступы между элементами
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category.title
                    // Чип категории
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp)) // Скругление углов
                            .background(
                                if (isSelected) Color(0xFF48B2E7) else Color.White // Разный цвет для выбранной/невыбранной
                            )
                            .clickable { selectedCategory = category.title } // Обработка нажатия
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.title,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else Color(0xFF333333),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Отображение индикатора загрузки или сетки товаров
        if (isLoading) {
            // Индикатор загрузки по центру экрана
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF48B2E7))
            }
        } else {
            // Сетка товаров в 2 колонки
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // Фиксированное количество колонок - 2
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 16.dp), // Отступ снизу
                horizontalArrangement = Arrangement.spacedBy(12.dp), // Горизонтальный отступ между карточками
                verticalArrangement = Arrangement.spacedBy(12.dp) // Вертикальный отступ между карточками
            ) {
                // Отображение отфильтрованных товаров
                items(filteredProducts, key = { it.id }) { product ->
                    // Область нажатия для перехода на детальный экран
                    Box(
                        modifier = Modifier.clickable {
                            navController.navigate("details/${product.id}") // Навигация с ID товара
                        }
                    ) {
                        // Карточка товара
                        CatalogProductCard(
                            product = product,
                            onToggleFavorite = ::toggleFavourite // Передаем функцию переключения избранного
                        )
                    }
                }
            }
        }
    }
}

/**
 * Компонент карточки товара для отображения в сетке каталога
 * @param product данные товара для отображения
 * @param onToggleFavorite колбэк для переключения статуса избранного
 */
@Composable
private fun CatalogProductCard(
    product: CatalogProduct,
    onToggleFavorite: (CatalogProduct, Boolean) -> Unit
) {
    // Локальное состояние избранного для немедленного обновления UI
    var isFavorite by remember(product.id) { mutableStateOf(product.isFavorite) }

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
                // Иконка сердца (избранное)
                Icon(
                    painter = painterResource(
                        id = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_favorite_border // Разная иконка в зависимости от статуса
                    ),
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFDD4B4B) else Color(0xFFB0B0B0), // Красный для избранного, серый для обычного
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            val newValue = !isFavorite
                            isFavorite = newValue // Мгновенное обновление UI
                            onToggleFavorite(product, newValue) // Вызов колбэка для отправки на сервер
                        }
                )
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
                    color = Color(0xFF48B2E7),
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

            // Нижний ряд с ценой и кнопкой добавления
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