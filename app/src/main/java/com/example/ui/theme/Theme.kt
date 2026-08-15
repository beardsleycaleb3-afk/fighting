package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TapoutDarkColorScheme = darkColorScheme(
    primary = TapoutCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B0010),
    onPrimaryContainer = TapoutOrange,
    secondary = TapoutOrange,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2A0012),
    onSecondaryContainer = TapoutGold,
    tertiary = TapoutNeonBlue,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = Color(0xFFEEEEEE),
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF200015),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TapoutDarkColorScheme,
        typography = Typography,
        content = content
    )
}
