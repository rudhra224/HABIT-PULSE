package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// Minimalist Dark Palette
// Deep obsidian/midnight base with crisp slate surfaces
// ==========================================
val DarkBackground = Color(0xFF090D16)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkSurfaceDim = Color(0xFF090D16)
val DarkSurfaceBright = Color(0xFF1E293B)
val DarkSurfaceContainerLowest = Color(0xFF06090F)
val DarkSurfaceContainerLow = Color(0xFF0D131F)
val DarkSurfaceContainer = Color(0xFF131D2E)
val DarkSurfaceContainerHigh = Color(0xFF1B283E)
val DarkSurfaceContainerHighest = Color(0xFF24334E)
val DarkOutline = Color(0xFF283852)
val DarkOutlineVariant = Color(0xFF1E2A3F)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextTertiary = Color(0xFF64748B)

// ==========================================
// Minimalist Light Palette
// Crisp off-white canvas with pure white elevated surfaces
// ==========================================
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightSurfaceDim = Color(0xFFECEFF5)
val LightSurfaceBright = Color(0xFFFFFFFF)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF8FAFC)
val LightSurfaceContainer = Color(0xFFF1F5F9)
val LightSurfaceContainerHigh = Color(0xFFE2E8F0)
val LightSurfaceContainerHighest = Color(0xFFCBD5E1)
val LightOutline = Color(0xFFCBD5E1)
val LightOutlineVariant = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)
val LightTextTertiary = Color(0xFF94A3B8)

// ==========================================
// Intentional Semantic Accents (Habits & Goals)
// ==========================================
val PrimaryIndigo = Color(0xFF4F46E5)       // Sophisticated minimal Indigo (Light)
val PrimaryIndigoLight = Color(0xFF818CF8)  // Luminous Electric Indigo (Dark)
val PrimaryIndigoContainerLight = Color(0xFFEEF2FF)
val PrimaryIndigoContainerDark = Color(0xFF312E81)

val EmeraldGreen = Color(0xFF10B981)        // Crisp Emerald for completions
val EmeraldGreenLight = Color(0xFF34D399)   // Luminous Emerald (Dark)
val EmeraldContainerLight = Color(0xFFD1FAE5)
val EmeraldContainerDark = Color(0xFF065F46)

val AmberGold = Color(0xFFF59E0B)           // Warm Amber for achievements/coins
val AmberGoldLight = Color(0xFFFBBF24)      // Luminous Gold (Dark)
val AmberContainerLight = Color(0xFFFEF3C7)
val AmberContainerDark = Color(0xFF78350F)

val CrimsonRose = Color(0xFFEF4444)         // Minimal Coral/Rose for errors/resets
val CrimsonRoseLight = Color(0xFFF87171)    // Soft Coral (Dark)
val CrimsonContainerLight = Color(0xFFFEE2E2)
val CrimsonContainerDark = Color(0xFF7F1D1D)

val ElectricSky = Color(0xFF38BDF8)         // Focus & clarity
val ElectricIndigo = Color(0xFF6366F1)      // Vibrant Indigo (Legacy alias)
val VioletPurple = Color(0xFF8B5CF6)        // Mindfulness & reflection
val CyanTeal = Color(0xFF06B6D4)            // Wellness & hydration
val FlameOrange = Color(0xFFF97316)         // Streak flame badge

// Preset Colors for Habit Selection
val HabitColors = listOf(
    "#6366F1", // Indigo
    "#10B981", // Emerald
    "#38BDF8", // Sky Blue
    "#F59E0B", // Amber
    "#EF4444", // Rose/Crimson
    "#8B5CF6", // Violet
    "#06B6D4", // Cyan
    "#EC4899"  // Pink
)

/**
 * Extended semantic tokens for HabitPulse gamification and habit attributes.
 */
@Immutable
data class HabitCustomTokens(
    val streakFlame: Color,
    val coinGold: Color,
    val xpViolet: Color,
    val freezeCyan: Color,
    val successEmerald: Color
)

val DarkHabitTokens = HabitCustomTokens(
    streakFlame = Color(0xFFFB923C),
    coinGold = Color(0xFFFBBF24),
    xpViolet = Color(0xFFA78BFA),
    freezeCyan = Color(0xFF38BDF8),
    successEmerald = Color(0xFF34D399)
)

val LightHabitTokens = HabitCustomTokens(
    streakFlame = Color(0xFFEA580C),
    coinGold = Color(0xFFD97706),
    xpViolet = Color(0xFF7C3AED),
    freezeCyan = Color(0xFF0284C7),
    successEmerald = Color(0xFF059669)
)

val LocalHabitTokens = staticCompositionLocalOf { LightHabitTokens }
