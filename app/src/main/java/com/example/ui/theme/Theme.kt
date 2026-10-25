package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GamepadColorScheme = darkColorScheme(
    primary = GamepadCyan,
    onPrimary = Color.Black,
    primaryContainer = GamepadSurfaceVariant,
    onPrimaryContainer = GamepadCyan,
    secondary = GamepadTextSecondary,
    onSecondary = Color.White,
    background = GamepadBackground,
    onBackground = GamepadTextPrimary,
    surface = GamepadSurface,
    onSurface = GamepadTextPrimary,
    surfaceVariant = GamepadSurfaceVariant,
    onSurfaceVariant = GamepadTextSecondary,
    outline = GamepadBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GamepadColorScheme,
        typography = Typography,
        content = content
    )
}
