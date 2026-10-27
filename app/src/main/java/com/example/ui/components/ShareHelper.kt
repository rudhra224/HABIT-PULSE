package com.example.ui.components

import android.content.Context
import android.content.Intent
import com.example.data.model.Habit
import com.example.data.model.UserProfile

object ShareHelper {

    fun shareHabitStreak(context: Context, habit: Habit, userProfile: UserProfile?) {
        val levelName = userProfile?.let { com.example.data.model.GamificationConfig.getLevelTitle(it.level) } ?: "Habit Builder"
        val shareText = buildString {
            append("🔥 STREAK MILESTONE!\n\n")
            append("I've maintained a ${habit.currentStreak}-day streak on \"${habit.title}\" with HabitPulse!\n")
            append("🏆 Total completions: ${habit.totalCompletions}\n")
            append("⚡ Level: ${userProfile?.level ?: 1} ($levelName)\n")
            append("🌟 Longest streak: ${habit.longestStreak} days\n\n")
            append("Tracking my consistency and building lifelong habits daily. #HabitPulse #HabitTracker #ConsistencyWins")
        }

        launchShareChooser(context, "Share Habit Streak", shareText)
    }

    fun shareMonthlyReport(
        context: Context,
        monthName: String,
        consistencyScore: Int,
        totalCompletions: Int,
        activeHabitsCount: Int,
        longestActiveStreak: Long,
        userProfile: UserProfile?
    ) {
        val levelName = userProfile?.let { com.example.data.model.GamificationConfig.getLevelTitle(it.level) } ?: "Habit Builder"
        val shareText = buildString {
            append("📊 MONTHLY HABIT REPORT — $monthName\n\n")
            append("🎯 Consistency Score: $consistencyScore%\n")
            append("✅ Habits Checked Off: $totalCompletions times\n")
            append("🔥 Peak Active Streak: $longestActiveStreak days\n")
            append("🌱 Active Habits Tracked: $activeHabitsCount\n")
            append("⚡ Current Rank: Level ${userProfile?.level ?: 1} ($levelName) with ${userProfile?.xp ?: 0} XP\n\n")
            append("Every day counts. Powered by HabitPulse! #HabitTracker #SelfImprovement #PersonalGrowth")
        }

        launchShareChooser(context, "Share Monthly Report", shareText)
    }

    fun shareBadgeUnlock(context: Context, badgeTitle: String, badgeDesc: String, userProfile: UserProfile?) {
        val shareText = buildString {
            append("🎖️ NEW ACHIEVEMENT UNLOCKED!\n\n")
            append("I just earned the \"$badgeTitle\" badge on HabitPulse!\n")
            append("\"$badgeDesc\"\n\n")
            append("Currently Level ${userProfile?.level ?: 1} with ${userProfile?.xp ?: 0} XP. Consistency is superpower! 🚀 #HabitPulse")
        }

        launchShareChooser(context, "Share Achievement", shareText)
    }

    private fun launchShareChooser(context: Context, title: String, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
