package com.example.ela.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Rose600,
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
    // Principal
    primary = Rose600,
    onPrimary = Color.White,

    // Container da cor principal
    primaryContainer = Rose100,
    onPrimaryContainer = Rose900,

    // Secundária
    secondary = Lavender600,
    onSecondary = Color.White,
    secondaryContainer = Lavender100,
    onSecondaryContainer = Lavender900,

    // Terciária
    tertiary = Coral500,
    onTertiary = Color.White,
    tertiaryContainer = Coral100,
    onTertiaryContainer = Coral900,

    // Fundo geral do aplicativo
    background = Color.White,
    onBackground = WarmGray900,

    // Superfícies / Cards
    surface = Color.White,
    onSurface = WarmGray900,

    // IMPORTANTE:
    // não usar Rose100 aqui
    surfaceVariant = WarmGray100,
    onSurfaceVariant = WarmGray700,

    // Erros
    error = Rose600,
    onError = Color.White,
    errorContainer = Rose100,
    onErrorContainer = Rose900,

    // Bordas
    outline = WarmGray400,
    outlineVariant = WarmGray200,

    // Superfície inversa
    inverseSurface = WarmGray800,
    inverseOnSurface = WarmGray50,

    // Cor principal inversa
    inversePrimary = Rose300,

    // Evita tonalização rosa nas superfícies
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
