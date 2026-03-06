package com.example.sneakersshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sneakersshop.data.UserSession
import com.example.sneakersshop.ui.theme.SneakersShopTheme
import com.example.sneakersshop.ui.view.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SneakersShopTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Сплэш-экран
                        composable("splash") {
                            SplashScreen(
                                onTimeout = {
                                    navController.navigate("onboarding") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Онбординг
                        composable("onboarding") {
                            OnboardingScreen(navController = navController)
                        }

                        // Авторизация
                        composable("login") {
                            LoginScreen(navController = navController)
                        }

                        composable("register") {
                            RegisterScreen(navController = navController)
                        }

                        composable("forgot_password") {
                            ForgotPasswordScreen(navController = navController)
                        }

                        // OTP подтверждение
                        composable(
                            route = "verifyOTP/{email}/{type}",
                            arguments = listOf(
                                navArgument("email") { type = NavType.StringType },
                                navArgument("type") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            val type = backStackEntry.arguments?.getString("type") ?: "signup"
                            VerifyOTPScreen(
                                navController = navController,
                                email = email,
                                otpType = type
                            )
                        }

                        // Установка нового пароля
                        composable(
                            route = "new_password/{email}",
                            arguments = listOf(
                                navArgument("email") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            NewPasswordScreen(
                                navController = navController,
                                email = email
                            )
                        }

                        // Главный экран
                        composable("home") {
                            HomeScreen(navController = navController)
                        }

                        // Профиль (только чтение)
                        composable("profile") {
                            val userId = UserSession.userId ?: ""
                            val accessToken = UserSession.accessToken ?: ""
                            ProfileScreen(
                                navController = navController,
                                userId = userId,
                                accessToken = accessToken
                            )
                        }

                        // Редактирование профиля
                        composable(
                            route = "editProfile/{firstName}/{lastName}/{address}/{phone}",
                            arguments = listOf(
                                navArgument("firstName") { type = NavType.StringType },
                                navArgument("lastName") { type = NavType.StringType },
                                navArgument("address") { type = NavType.StringType },
                                navArgument("phone") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val firstName = backStackEntry.arguments?.getString("firstName") ?: ""
                            val lastName = backStackEntry.arguments?.getString("lastName") ?: ""
                            val address = backStackEntry.arguments?.getString("address") ?: ""
                            val phone = backStackEntry.arguments?.getString("phone") ?: ""
                            EditProfileScreen(
                                navController = navController,
                                initialFirstName = firstName,
                                initialLastName = lastName,
                                initialAddress = address,
                                initialPhone = phone,
                                userId = UserSession.userId ?: "",
                                accessToken = UserSession.accessToken ?: ""
                            )
                        }

                        // Избранное
                        composable("favorite") {
                            FavoriteScreen(navController = navController)
                        }

                        // Каталог с категорией
                        composable(
                            route = "catalog/{category}",
                            arguments = listOf(
                                navArgument("category") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val category = backStackEntry.arguments?.getString("category") ?: "Outdoor"
                            CatalogScreen(
                                navController = navController,
                                initialCategoryTitle = category
                            )
                        }

                        // Каталог без параметра
                        composable("catalog") {
                            CatalogScreen(
                                navController = navController,
                                initialCategoryTitle = "Outdoor"
                            )
                        }

                        // Детали товара
                        composable(
                            route = "details/{productId}",
                            arguments = listOf(
                                navArgument("productId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getString("productId") ?: ""
                            DetailsScreen(
                                navController = navController,
                                productId = productId
                            )
                        }
                    }
                }
            }
        }
    }
}