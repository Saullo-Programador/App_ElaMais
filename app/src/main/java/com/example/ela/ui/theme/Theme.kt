package com.example.ela.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Rose300,
    onPrimary = Rose900,
    primaryContainer = Rose700,
    onPrimaryContainer = Rose100,
    secondary = Lavender300,
    onSecondary = Lavender900,
    secondaryContainer = Lavender700,
    onSecondaryContainer = Lavender100,
    tertiary = Coral300,
    onTertiary = Coral900,
    tertiaryContainer = Coral700,
    onTertiaryContainer = Coral100,
    background = WarmGray900,
    onBackground = WarmGray100,
    surface = WarmGray800,
    onSurface = WarmGray100,
    surfaceVariant = WarmGray700,
    onSurfaceVariant = WarmGray300,
    error = Rose500,
    onError = WarmGray50,
    errorContainer = Rose900,
    onErrorContainer = Rose100,
    outline = WarmGray500,
    outlineVariant = WarmGray700,
    inverseSurface = WarmGray100,
    inverseOnSurface = WarmGray900,
    inversePrimary = Rose400,
    surfaceTint = Rose300,
    scrim = WarmGray900
)

private val LightColorScheme = lightColorScheme(
    primary = Rose600,
    onPrimary = WarmGray50,
    primaryContainer = Rose100,
    onPrimaryContainer = Rose900,
    secondary = Lavender600,
    onSecondary = WarmGray50,
    secondaryContainer = Lavender100,
    onSecondaryContainer = Lavender900,
    tertiary = Coral500,
    onTertiary = WarmGray50,
    tertiaryContainer = Coral100,
    onTertiaryContainer = Coral900,
    background = Color.White,
    onBackground = WarmGray900,
    surface = WarmGray50,
    onSurface = WarmGray900,
    surfaceVariant = Rose100,
    onSurfaceVariant = WarmGray700,
    error = Rose600,
    onError = WarmGray50,
    errorContainer = Rose100,
    onErrorContainer = Rose900,
    outline = WarmGray400,
    outlineVariant = WarmGray200,
    inverseSurface = WarmGray800,
    inverseOnSurface = WarmGray50,
    inversePrimary = Rose300,
    surfaceTint = Rose600,
    scrim = WarmGray900
)

@Composable
fun ElaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desativamos cores dinâmicas para manter a identidade visual
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
