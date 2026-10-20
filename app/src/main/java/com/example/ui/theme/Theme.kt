package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ParsPingColorScheme = darkColorScheme(
    primary = ParsElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ParsNavyCard,
    onPrimaryContainer = ParsCyanNeon,
    secondary = ParsCyanNeon,
    onSecondary = ParsNavyDark,
    background = ParsNavyDark,
    onBackground = ParsTextPrimary,
    surface = ParsNavySurface,
    onSurface = ParsTextPrimary,
    surfaceVariant = ParsNavyCard,
    onSurfaceVariant = ParsTextSecondary,
    outline = ParsNavyCardBorder,
    error = ParsErrorRed,
    onError = Color.White
)

@Composable
fun ParsPingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ParsPingColorScheme,
        typography = Typography,
        content = content
    )
}
