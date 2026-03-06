package com.example.sneakersshop.ui.view

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.sneakersshop.ui.viewModel.SignUpViewModel

@Composable
fun RegisterButton(
    name: String,
    email: String,
    password: String,
    isTermsAccepted: Boolean,
    viewModel: SignUpViewModel = viewModel(),
    navController: NavHostController
) {
    val context = LocalContext.current

    fun validateInputs(): Boolean {
        if (name.isBlank()) {
            Toast.makeText(context, "Введите имя", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Некорректный email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(context, "Пароль должен быть минимум 6 символов", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isTermsAccepted) {
            Toast.makeText(context, "Необходимо согласие на обработку данных", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    Button(
        onClick = {
            if (validateInputs()) {
                // Сохраняем email в SharedPreferences (если нужно для OTP)
                val prefs = context.getSharedPreferences("my_app_preferences", Context.MODE_PRIVATE)
                prefs.edit().putString("userEmail", email.trim()).apply()

                // Вызов метода регистрации в ViewModel
                viewModel.signUp(name, email.trim(), password.trim(), navController)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF48B2E7),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF2B6B8B),
            disabledContentColor = Color.White
        ),
        enabled = !viewModel.isLoading.value
    ) {
        if (viewModel.isLoading.value) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Зарегистрироваться",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}