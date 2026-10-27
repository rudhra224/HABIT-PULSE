package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {

    @Test
    fun lightColorScheme_hasValidMinimalistTokens() {
        val light = LightColorScheme

        // Background & surface
        assertEquals(LightBackground, light.background)
        assertEquals(LightSurface, light.surface)
        assertEquals(LightTextPrimary, light.onBackground)
        assertEquals(LightTextPrimary, light.onSurface)

        // Primary & contrast
        assertEquals(PrimaryIndigo, light.primary)
        assertEquals(Color.White, light.onPrimary)

        // Secondary & tertiary
        assertEquals(EmeraldGreen, light.secondary)
        assertEquals(AmberGold, light.tertiary)

        // Error & outline
        assertEquals(CrimsonRose, light.error)
        assertEquals(LightOutline, light.outline)
    }

    @Test
    fun darkColorScheme_hasValidMinimalistTokens() {
        val dark = DarkColorScheme

        // Background & surface
        assertEquals(DarkBackground, dark.background)
        assertEquals(DarkSurface, dark.surface)
        assertEquals(DarkTextPrimary, dark.onBackground)
        assertEquals(DarkTextPrimary, dark.onSurface)

        // Primary & contrast
        assertEquals(PrimaryIndigoLight, dark.primary)
        assertEquals(Color(0xFF0F172A), dark.onPrimary)

        // Secondary & tertiary
        assertEquals(EmeraldGreenLight, dark.secondary)
        assertEquals(AmberGoldLight, dark.tertiary)

        // Error & outline
        assertEquals(CrimsonRoseLight, dark.error)
        assertEquals(DarkOutline, dark.outline)
    }

    @Test
    fun lightAndDarkSchemes_areDistinct() {
        assertNotEquals(LightColorScheme.background, DarkColorScheme.background)
        assertNotEquals(LightColorScheme.surface, DarkColorScheme.surface)
        assertNotEquals(LightColorScheme.primary, DarkColorScheme.primary)
        assertNotEquals(LightColorScheme.onSurface, DarkColorScheme.onSurface)
    }

    @Test
    fun typography_hasConfiguredStyles() {
        assertNotNull(Typography.displayLarge)
        assertNotNull(Typography.headlineLarge)
        assertNotNull(Typography.titleLarge)
        assertNotNull(Typography.titleMedium)
        assertNotNull(Typography.bodyLarge)
        assertNotNull(Typography.bodyMedium)
        assertNotNull(Typography.labelLarge)
        assertNotNull(Typography.labelSmall)
    }

    @Test
    fun shapes_hasConfiguredTiers() {
        assertNotNull(Shapes.extraSmall)
        assertNotNull(Shapes.small)
        assertNotNull(Shapes.medium)
        assertNotNull(Shapes.large)
        assertNotNull(Shapes.extraLarge)
    }

    @Test
    fun habitCustomTokens_areConfigured() {
        assertNotNull(LightHabitTokens.streakFlame)
        assertNotNull(LightHabitTokens.coinGold)
        assertNotNull(LightHabitTokens.xpViolet)
        assertNotNull(LightHabitTokens.freezeCyan)

        assertNotNull(DarkHabitTokens.streakFlame)
        assertNotNull(DarkHabitTokens.coinGold)
        assertNotNull(DarkHabitTokens.xpViolet)
        assertNotNull(DarkHabitTokens.freezeCyan)

        // Verify habit colors list is populated
        assertTrue(HabitColors.isNotEmpty())
        assertEquals(8, HabitColors.size)
    }

    @Test
    fun themeMode_containsAllModes() {
        val modes = ThemeMode.entries
        assertEquals(3, modes.size)
        assertTrue(modes.contains(ThemeMode.SYSTEM))
        assertTrue(modes.contains(ThemeMode.DARK))
        assertTrue(modes.contains(ThemeMode.LIGHT))
    }
}
