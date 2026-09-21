package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonCyanLight,
    onPrimary = CharcoalDark,
    primaryContainer = CharcoalCard,
    onPrimaryContainer = NeonCyanLight,
    secondary = NeonVioletLight,
    onSecondary = CharcoalDark,
    secondaryContainer = CharcoalCard,
    onSecondaryContainer = NeonVioletLight,
    tertiary = NeonPink,
    onTertiary = CharcoalDark,
    background = CharcoalDark,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextSecondary,
    outline = CharcoalCardBorder,
    error = NeonRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to futuristic dark theme
  dynamicColor: Boolean = false, // Keep cohesive sci-fi branding
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

