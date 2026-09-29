package com.N170_Manassingh.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisNeonCyan,
    onPrimary = JarvisBackground,
    primaryContainer = JarvisTranslucentPanel,
    onPrimaryContainer = JarvisTextPrimary,
    
    secondary = JarvisTextSecondary,
    onSecondary = JarvisBackground,
    secondaryContainer = JarvisSurface,
    onSecondaryContainer = JarvisTextPrimary,
    
    tertiary = JarvisDarkCyan,
    onTertiary = JarvisTextPrimary,
    
    error = JarvisError,
    errorContainer = Color(0x33FF1744), // Translucent red
    onError = JarvisBackground,
    onErrorContainer = JarvisTextPrimary,
    
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisBackground,
    onSurfaceVariant = JarvisTextSecondary,
    
    outline = JarvisNeonCyan
)

@Composable
fun GeminiApiComposeStarterTheme(
    darkTheme: Boolean = true, // Force Dark Theme for Jarvis
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the neon palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
