package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Supported theme modes for user preference selection.
 */
enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

/**
 * Minimalist Dark Material 3 Color Scheme.
 * Built on a deep obsidian foundation with crisp slate surfaces and luminous semantic accents.
 */
val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = PrimaryIndigoLight,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = PrimaryIndigoContainerDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    inversePrimary = PrimaryIndigo,

    secondary = EmeraldGreenLight,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = Color(0xFFA7F3D0),

    tertiary = AmberGoldLight,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = Color(0xFFFDE68A),

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceTint = PrimaryIndigoLight,

    surfaceBright = DarkSurfaceBright,
    surfaceDim = DarkSurfaceDim,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,

    inverseSurface = LightSurface,
    inverseOnSurface = LightTextPrimary,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,

    error = CrimsonRoseLight,
    onError = Color(0xFF450A0A),
    errorContainer = CrimsonContainerDark,
    onErrorContainer = Color(0xFFFECACA),

    scrim = Color.Black
)

/**
 * Minimalist Light Material 3 Color Scheme.
 * Built on a crisp slate off-white canvas with elevated white surfaces and refined indigo/emerald accents.
 */
val LightColorScheme: ColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoContainerLight,
    onPrimaryContainer = Color(0xFF1E1B4B),
    inversePrimary = PrimaryIndigoLight,

    secondary = EmeraldGreen,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = Color(0xFF064E3B),

    tertiary = AmberGold,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = Color(0xFF78350F),

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = PrimaryIndigo,

    surfaceBright = LightSurfaceBright,
    surfaceDim = LightSurfaceDim,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,

    inverseSurface = DarkSurface,
    inverseOnSurface = DarkTextPrimary,

    outline = LightOutline,
    outlineVariant = LightOutlineVariant,

    error = CrimsonRose,
    onError = Color.White,
    errorContainer = CrimsonContainerLight,
    onErrorContainer = Color(0xFF7F1D1D),

    scrim = Color.Black
)

/**
 * Custom habit tokens accessor on MaterialTheme.
 */
val MaterialTheme.habitTokens: HabitCustomTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalHabitTokens.current

/**
 * Primary HabitPulse theme wrapper supporting Light, Dark, and System modes.
 */
@Composable
fun HabitPulseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val habitTokens = if (darkTheme) DarkHabitTokens else LightHabitTokens

    CompositionLocalProvider(LocalHabitTokens provides habitTokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
