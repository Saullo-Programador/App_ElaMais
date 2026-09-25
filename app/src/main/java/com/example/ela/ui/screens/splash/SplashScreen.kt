package com.example.ela.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ela.ui.theme.Rose200
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ela.R
import com.example.ela.ui.navigation.Screen
import com.example.ela.ui.screens.auth.LoginViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: LoginViewModel = hiltViewModel()
) {
    var logoStep by remember { mutableIntStateOf(0) } // 0: logo1, 1: logo2

    // Animations
    val scale by animateFloatAsState(
        targetValue = if (logoStep == 0) 1.0f else 1.1f,
        animationSpec = tween(durationMillis = 1000),
        label = "logoScale"
    )

    val alpha by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = tween(durationMillis = 800),
        label = "logoAlpha"
    )

    LaunchedEffect(Unit) {
        // Step 1: Show Logo 1
        logoStep = 0
        kotlinx.coroutines.delay(1500)

        // Step 2: Transition to Logo 2
        logoStep = 1
        kotlinx.coroutines.delay(1500)

        // Step 3: Navigate to appropriate screen
        val isAuthenticated = authViewModel.isUserAuthenticated()
        val destination = if (isAuthenticated) Screen.Home.route else Screen.Login.route

        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val isDarkTheme = isSystemInDarkTheme()
    val gradientColors = if (isDarkTheme) {
        listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.surface
        )
    } else {
        listOf(
            Rose200.copy(alpha = 0.6f),
            Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .alpha(alpha)
        ) {
            if (logoStep == 0) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.logo2_elamais_removebg_preview),
                    contentDescription = "ElaMais Logo 1",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.logo2_elamais_removebg_preview),
                    contentDescription = "ElaMais Logo 2",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
