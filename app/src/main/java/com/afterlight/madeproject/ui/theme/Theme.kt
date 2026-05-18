package com.afterlight.madeproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Sand,
    onPrimary = Coal,
    secondary = DarkTextMuted,
    tertiary = Copper,
    onSecondary = DarkTextPrimary,
    background = Coal,
    onBackground = DarkTextPrimary,
    surface = Slate,
    surfaceVariant = DarkGlass,
    onSurface = DarkTextPrimary,
    error = Color(0xFFE89A7E)
)

private val LightColorScheme = lightColorScheme(
    primary = Copper,
    onPrimary = Snow,
    secondary = LightTextMuted,
    tertiary = Moss,
    onSecondary = LightTextPrimary,
    background = Snow,
    onBackground = LightTextPrimary,
    surface = Pearl,
    surfaceVariant = LightGlass,
    onSurface = LightTextPrimary,
    error = Wine
)

private val JournalShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(34.dp)
)

@Composable
fun GatherTheme(
    darkTheme: Boolean = false, // Forced light mode for high-contrast aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GatherTypography,
        shapes = JournalShapes,
        content = content
    )
}
