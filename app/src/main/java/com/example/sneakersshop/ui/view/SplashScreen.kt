package com.example.sneakersshop.ui.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sneakersshop.R
import com.example.sneakersshop.ui.theme.SplashBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splas),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
        )
    }
}