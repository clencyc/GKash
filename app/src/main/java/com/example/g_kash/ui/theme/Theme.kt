package com.example.g_kash.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Pink Accent Theme - Dark Mode
private val DarkColorScheme = darkColorScheme(
    // Primary - Pink accent with high contrast for dark mode
    primary = PinkPrimaryDark,              // #C11C84 - Dark Pink
    onPrimary = OnPinkPrimaryDark,          // White
    primaryContainer = PinkPrimaryContainer, // Dark Pink Container
    onPrimaryContainer = OnPinkPrimaryContainer,
    
    // Secondary - Muted Gold (complementary to pink)
    secondary = GoldSecondaryDark,          // Darker Gold for dark mode
    onSecondary = OnGoldSecondaryDark,      // Black
    secondaryContainer = GoldSecondaryContainer,
    onSecondaryContainer = OnGoldSecondaryContainer,
    
    // Tertiary - Keep yellow/amber for financial contexts
    tertiary = YellowAccent,
    onTertiary = OnYellowAccent,
    tertiaryContainer = YellowAccentContainer,
    onTertiaryContainer = OnYellowAccentContainer,
    
    // Background - Charcoal #121212 for reduced eye strain
    background = DarkBackground,            // #121212
    onBackground = OnDarkBackground,        // White
    surface = DarkSurface,                  // #1E1E1E - Dark Gray cards
    onSurface = OnDarkSurface,              // Off-white #F5F5F5
    surfaceVariant = DarkSurfaceVariant,    // Neutral dark gray
    onSurfaceVariant = OnDarkSurfaceVariant,
    
    error = ErrorColor,
    onError = OnErrorColor,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

// Pink Accent Theme - Light Mode
private val LightColorScheme = lightColorScheme(
    // Primary - Vibrant Pink for light mode
    primary = PinkPrimary,                  // #E91E63 - Vibrant Pink
    onPrimary = OnPinkPrimary,              // White
    primaryContainer = PinkPrimaryContainerLight, // Light Pink Container
    onPrimaryContainer = OnPinkPrimaryContainerLight,
    
    // Secondary - Muted Gold (complementary to pink)
    secondary = GoldSecondary,              // #EFBF04 - Muted Gold
    onSecondary = OnGoldSecondary,          // Black
    secondaryContainer = GoldSecondaryContainerLight,
    onSecondaryContainer = OnGoldSecondaryContainerLight,
    
    // Tertiary - Keep yellow/amber for financial contexts
    tertiary = YellowAccent,
    onTertiary = OnYellowAccent,
    tertiaryContainer = YellowAccentContainerLight,
    onTertiaryContainer = OnYellowAccentContainerLight,
    
    // Background - Pure white with enhanced contrast
    background = LightBackground,           // #FFFFFF - Pure White
    onBackground = OnLightBackground,       // Near Black #1C1B1F
    surface = LightSurface,                 // Near White #FCFCFC
    onSurface = OnLightSurface,             // Near Black
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightSurfaceVariant,
    
    error = ErrorColor,
    onError = OnErrorColor,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun GKashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as Activity).window
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            } catch (e: Exception) {
                // Handle gracefully if WindowCompat is not available
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alternative theme for financial contexts (green-focused)
@Composable
fun GKashFinancialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = FinancialGreen,
            primaryContainer = FinancialGreenContainer,
            secondary = FinancialBlue,
            tertiary = FinancialGold
        )
    } else {
        LightColorScheme.copy(
            primary = FinancialGreen,
            primaryContainer = FinancialGreenContainerLight,
            secondary = FinancialBlue,
            tertiary = FinancialGold
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Pink-focused theme for modern UI elements
@Composable
fun GKashPinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme // Already configured with pink accent
    } else {
        LightColorScheme // Already configured with pink accent
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as Activity).window
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            } catch (e: Exception) {
                // Handle gracefully if WindowCompat is not available
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
