package com.afterlight.madeproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = DarkSand,
    onPrimary = DarkCoal,
    primaryContainer = DarkPearl,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkTextMuted,
    onSecondary = DarkCoal,
    secondaryContainer = DarkGlass,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkCopper,
    onTertiary = DarkCoal,
    tertiaryContainer = DarkGlass,
    onTertiaryContainer = DarkTextPrimary,
    background = DarkSnow,
    onBackground = DarkTextPrimary,
    surface = DarkPearl,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSlate,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkHairline,
    error = Color(0xFFE89A7E)
)

private val LightColorScheme = lightColorScheme(
    primary = LightCopper,
    onPrimary = LightSnow,
    primaryContainer = LightPearl,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightTextMuted,
    onSecondary = LightSnow,
    secondaryContainer = LightGlass,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightMoss,
    onTertiary = LightSnow,
    tertiaryContainer = LightPearl,
    onTertiaryContainer = LightTextPrimary,
    background = LightSnow,
    onBackground = LightTextPrimary,
    surface = LightPearl,
    onSurface = LightTextPrimary,
    surfaceVariant = LightGlass,
    onSurfaceVariant = LightTextMuted,
    outline = LightHairline,
    error = LightWine
)

private val JournalShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

val LocalGatherDarkTheme = staticCompositionLocalOf { false }

@Composable
fun GatherTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val useDarkTheme = darkTheme ?: isSystemInDarkTheme()
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalGatherDarkTheme provides useDarkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GatherTypography,
            shapes = JournalShapes,
            content = content
        )
    }
}
