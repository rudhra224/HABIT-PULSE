package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val level: Long = 1L,
    val xp: Long = 0L,
    val streakFreezes: Long = 1L,
    val badges: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Habit(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "General",
    val colorHex: String = "#6366F1",
    val iconName: String = "star",
    val frequency: String = "daily", // daily, weekdays, weekends, custom
    val targetDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon, 7=Sun
    val targetCount: Long = 1L,
    val unit: String = "times",
    val currentStreak: Long = 0L,
    val longestStreak: Long = 0L,
    val totalCompletions: Long = 0L,
    val reminderTime: String = "",
    @get:PropertyName("isArchived") @set:PropertyName("isArchived") var isArchived: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class HabitLog(
    val id: String = "",
    val userId: String = "",
    val habitId: String = "",
    val date: String = "", // YYYY-MM-DD
    val completed: Boolean = false,
    val count: Long = 0L,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class BadgeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val requiredStreak: Int = 0,
    val requiredCompletions: Int = 0,
    val requiredLevel: Int = 0
)

object GamificationConfig {
    const val XP_PER_COMPLETION = 20L
    const val XP_ALL_COMPLETED_BONUS = 50L
    const val XP_PER_STREAK_MILESTONE = 100L
    const val STREAK_FREEZE_COST_XP = 150L

    val BADGES = listOf(
        BadgeDefinition("first_step", "First Step", "Complete your very first habit", "flag", requiredCompletions = 1),
        BadgeDefinition("three_day_streak", "3-Day Flame", "Maintain a streak of 3 consecutive days", "local_fire_department", requiredStreak = 3),
        BadgeDefinition("week_warrior", "Week Warrior", "Hit a 7-day uninterrupted streak", "bolt", requiredStreak = 7),
        BadgeDefinition("fortnight_fortitude", "14-Day Iron Will", "Crush two solid weeks of consistency", "shield", requiredStreak = 14),
        BadgeDefinition("month_master", "Month Master", "Reach a 30-day streak milestone", "military_tech", requiredStreak = 30),
        BadgeDefinition("century_club", "Century Club", "Log 100 total habit completions", "emoji_events", requiredCompletions = 100),
        BadgeDefinition("habit_architect", "Habit Architect", "Create 5 or more active habits", "construction", requiredCompletions = 5),
        BadgeDefinition("level_five", "Ascendant", "Attain Level 5 status in HabitPulse", "stars", requiredLevel = 5)
    )

    fun calculateLevel(xp: Long): Pair<Long, Float> {
        // Level curve: Level 1: 0-99 XP, Level 2: 100-249 XP, Level 3: 250-449 XP, etc.
        // Formula: level = 1 + floor(sqrt(xp / 50))
        var lvl = 1L
        var threshold = 0L
        var nextThreshold = 100L

        while (xp >= nextThreshold) {
            lvl++
            threshold = nextThreshold
            nextThreshold += (lvl * 100L)
        }

        val range = (nextThreshold - threshold).coerceAtLeast(1L)
        val currentProgress = (xp - threshold).coerceAtLeast(0L)
        val progressPercent = (currentProgress.toFloat() / range.toFloat()).coerceIn(0f, 1f)

        return Pair(lvl, progressPercent)
    }

    fun getLevelTitle(level: Long): String {
        return when (level) {
            1L -> "Novice"
            2L -> "Spark"
            3L -> "Momentum"
            4L -> "Consistency"
            5L -> "Habit Builder"
            6L -> "Unstoppable"
            7L -> "Iron Disciplined"
            8L -> "Champion"
            9L -> "Grandmaster"
            else -> "Zen Master"
        }
    }
}
