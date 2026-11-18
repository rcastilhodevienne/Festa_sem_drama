package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class FestaSemanticColors(
    val cardBorder: Color,
    val subtleSurface: Color,
    val emeraldText: Color,
    val emeraldContainer: Color,
    val amberText: Color,
    val amberContainer: Color,
    val roseText: Color,
    val roseContainer: Color,
    val indigoText: Color,
    val indigoContainer: Color,
    val mutedText: Color
)

private val LightFestaColors = FestaSemanticColors(
    cardBorder = Slate200,
    subtleSurface = Slate100,
    emeraldText = Emerald700,
    emeraldContainer = Emerald50,
    amberText = Amber700,
    amberContainer = Amber50,
    roseText = Rose700,
    roseContainer = Rose50,
    indigoText = Indigo700,
    indigoContainer = Indigo50,
    mutedText = Slate500
)

private val DarkFestaColors = FestaSemanticColors(
    cardBorder = Zinc800,
    subtleSurface = Zinc800,
    emeraldText = Emerald400,
    emeraldContainer = Emerald900.copy(alpha = 0.45f),
    amberText = Amber400,
    amberContainer = Amber900.copy(alpha = 0.45f),
    roseText = Rose400,
    roseContainer = Rose900.copy(alpha = 0.45f),
    indigoText = Indigo400,
    indigoContainer = Indigo900.copy(alpha = 0.45f),
    mutedText = Slate400
)

val LocalFestaColors = staticCompositionLocalOf { LightFestaColors }

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo700,
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Emerald50,
    onSecondaryContainer = Emerald700,
    tertiary = Amber600,
    onTertiary = Color.White,
    tertiaryContainer = Amber50,
    onTertiaryContainer = Amber700,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Slate300,
    error = Rose600,
    onError = Color.White,
    errorContainer = Rose50,
    onErrorContainer = Rose700
)

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo900,
    onPrimaryContainer = Indigo100,
    secondary = Emerald400,
    onSecondary = Zinc950,
    secondaryContainer = Emerald900,
    onSecondaryContainer = Emerald100,
    tertiary = Amber400,
    onTertiary = Zinc950,
    tertiaryContainer = Amber900,
    onTertiaryContainer = Amber100,
    background = Zinc950,
    onBackground = Slate50,
    surface = Zinc900,
    onSurface = Slate50,
    surfaceVariant = Zinc800,
    onSurfaceVariant = Slate300,
    outline = Zinc800,
    outlineVariant = Slate700,
    error = Rose400,
    onError = Zinc950,
    errorContainer = Rose900,
    onErrorContainer = Rose100
)

val FestaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FestaSemDramaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) DarkFestaColors else LightFestaColors

    CompositionLocalProvider(LocalFestaColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FestaTypography,
            shapes = FestaShapes,
            content = content
        )
    }
}
