package com.amigo.ticketbooker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom purple colors for Quick Tatkal theme
private val PurplePrimary = Color(0xFF6200EA)
private val PurpleSecondary = Color(0xFF9D46FF)
private val PurpleBackground = Color(0xFF121212)
private val PurpleSurface = Color(0xFF1E1E1E)
private val PurpleSurfaceVariant = Color(0xFF2D2D2D)
private val PurplePrimaryContainer = Color(0xFF3700B3)

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePrimaryContainer,
    onPrimaryContainer = Color.White,
    secondary = PurpleSecondary,
    onSecondary = Color.White,
    background = PurpleBackground,
    onBackground = Color.White,
    surface = PurpleSurface,
    onSurface = Color.White,
    surfaceVariant = PurpleSurfaceVariant,
    onSurfaceVariant = Color.White.copy(alpha = 0.8f),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

// We'll use the dark theme as the default for this app
@Composable
fun QuickTatkalTheme(
    darkTheme: Boolean = true, // Always use dark theme by default
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicDarkColorScheme(context)
        }
        else -> DarkColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Modern way to handle system bars
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Set the system bars to be transparent and visible
            val controller = WindowCompat.getInsetsController(window, view)
            controller.apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
