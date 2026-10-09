package com.example.santune.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ============================================================
// DARK COLOR SCHEME
// ============================================================

private val DarkColors = darkColorScheme(

    primary = PlayButton,
    onPrimary = SanTuneWhite,

    secondary = ActiveControl,
    onSecondary = SanTuneBlack,

    tertiary = NavyBlueLight,
    onTertiary = SanTuneBlack,

    background = NavyBackground,
    onBackground = NavyText,

    surface = NavySurface,
    onSurface = NavyText,

    surfaceVariant = NavySurfaceLight,
    onSurfaceVariant = NavyTextSecondary,

    outline = NavyDivider
)


// ============================================================
// LIGHT COLOR SCHEME
// ============================================================

private val LightColors = lightColorScheme(

    primary = LightAccent,
    onPrimary = SanTuneWhite,

    secondary = LightAccentPressed,
    onSecondary = SanTuneWhite,

    tertiary = LightAccent,
    onTertiary = SanTuneWhite,

    background = LightBackground,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,

    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,

    outline = LightDivider
)


// ============================================================
// SAN TUNE THEME
// ============================================================

@Composable
fun SanTuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors =
        if (darkTheme) {
            DarkColors
        } else {
            LightColors
        }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}