package com.rudra.isptechniciantool.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Professional Shape System
val ISPShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Elevation System
object ISPElevation {
    val None = 0.dp
    val ExtraLow = 1.dp
    val Low = 2.dp
    val Medium = 4.dp
    val High = 8.dp
    val ExtraHigh = 12.dp
}

// Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = TechBlue,
    onPrimary = NeutralWhite,
    primaryContainer = TechLighterBlue,
    onPrimaryContainer = TechDarkBlue,
    secondary = TechLightBlue,
    onSecondary = TechDarkBlue,
    secondaryContainer = TechLighterBlue,
    onSecondaryContainer = TechDarkBlue,
    tertiary = SuccessGreen,
    onTertiary = NeutralWhite,
    tertiaryContainer = SuccessLightGreen,
    onTertiaryContainer = SuccessGreen,
    error = ErrorRed,
    onError = NeutralWhite,
    errorContainer = ErrorLightRed,
    onErrorContainer = ErrorRed,
    background = BackgroundPrimary,
    onBackground = NeutralBlack,
    surface = BackgroundSecondary,
    onSurface = NeutralBlack,
    surfaceVariant = NeutralLightGray,
    onSurfaceVariant = NeutralDarkGray,
    outline = NeutralGray,
    outlineVariant = NeutralLightGray,
    scrim = NeutralBlack,
    inverseSurface = NeutralDarkGray,
    inverseOnSurface = NeutralWhite,
    inversePrimary = TechLighterBlue
)

// Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = TechLightBlue,
    onPrimary = TechDarkBlue,
    primaryContainer = TechDarkBlue,
    onPrimaryContainer = TechLighterBlue,
    secondary = TechBlue,
    onSecondary = NeutralWhite,
    secondaryContainer = TechDarkBlue,
    onSecondaryContainer = TechLighterBlue,
    tertiary = SuccessGreen,
    onTertiary = NeutralWhite,
    tertiaryContainer = SuccessGreen,
    onTertiaryContainer = SuccessLightGreen,
    error = ErrorRed,
    onError = NeutralWhite,
    errorContainer = ErrorRed,
    onErrorContainer = ErrorLightRed,
    background = BackgroundDark,
    onBackground = NeutralWhite,
    surface = SurfaceDark,
    onSurface = NeutralWhite,
    surfaceVariant = NeutralDarkGray,
    onSurfaceVariant = NeutralLightGray,
    outline = NeutralGray,
    outlineVariant = NeutralDarkGray,
    scrim = NeutralBlack,
    inverseSurface = NeutralWhite,
    inverseOnSurface = NeutralBlack,
    inversePrimary = TechBlue
)

@Composable
fun ISPTechnicianToolTheme(
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
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ISPTypography,
        shapes = ISPShapes,
        content = content
    )
}
