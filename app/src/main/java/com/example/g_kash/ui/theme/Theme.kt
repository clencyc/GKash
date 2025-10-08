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

// Custom color palette for financial app
private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = OnGreenPrimary,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = OnGreenPrimaryContainer,
    
    secondary = BluePrimary,
    onSecondary = OnBluePrimary,
    secondaryContainer = BlueSecondaryContainer,
    onSecondaryContainer = OnBlueSecondaryContainer,
    
    tertiary = YellowAccent,
    onTertiary = OnYellowAccent,
    tertiaryContainer = YellowAccentContainer,
    onTertiaryContainer = OnYellowAccentContainer,
    
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    
    error = ErrorColor,
    onError = OnErrorColor,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = OnGreenPrimary,
    primaryContainer = GreenPrimaryContainerLight,
    onPrimaryContainer = OnGreenPrimaryContainerLight,
    
    secondary = BluePrimary,
    onSecondary = OnBluePrimary,
    secondaryContainer = BlueSecondaryContainerLight,
    onSecondaryContainer = OnBlueSecondaryContainerLight,
    
    tertiary = YellowAccent,
    onTertiary = OnYellowAccent,
    tertiaryContainer = YellowAccentContainerLight,
    onTertiaryContainer = OnYellowAccentContainerLight,
    
    background = LightBackground,
    onBackground = OnLightBackground,
    surface = LightSurface,
    onSurface = OnLightSurface,
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

// Custom theme for financial contexts (green-focused)
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
