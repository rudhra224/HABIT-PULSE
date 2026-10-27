package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HabitUiState
import com.example.ui.components.DayCompletion
import com.example.ui.components.MonthlyCalendarGrid
import com.example.ui.components.MonthlyReportCard
import com.example.ui.components.ShareHelper
import com.example.ui.components.WeeklyBarChart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsScreen(
    uiState: HabitUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    // Generate weekly data for the current week (Mon-Sun)
    val weeklyData = remember(uiState.allLogs, uiState.habits) {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayLabelFormat = SimpleDateFormat("E", Locale.US)

        val list = mutableListOf<DayCompletion>()
        val totalActiveHabits = uiState.habits.size.coerceAtLeast(1)

        for (i in 0..6) {
            val dateStr = dayFormat.format(cal.time)
            val label = dayLabelFormat.format(cal.time).take(1)
            val completed = uiState.allLogs.count { it.date == dateStr && it.completed }
            list.add(DayCompletion(label, dateStr, completed, totalActiveHabits))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    // Monthly data & consistency calculation
    val monthPrefix = remember(selectedYear, selectedMonth) {
        String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonth + 1)
    }

    val monthLogs = remember(uiState.allLogs, monthPrefix) {
        uiState.allLogs.filter { it.date.startsWith(monthPrefix) }
    }

    val totalMonthCompletions = remember(monthLogs) {
        monthLogs.count { it.completed }
    }

    val daysWithLogs = remember(monthLogs) {
        val map = mutableMapOf<Int, Int>()
        monthLogs.filter { it.completed }.forEach { log ->
            val dayPart = log.date.substringAfterLast("-").toIntOrNull()
            if (dayPart != null) {
                map[dayPart] = (map[dayPart] ?: 0) + 1
            }
        }
        map
    }

    val longestActiveStreak = remember(uiState.habits) {
        uiState.habits.maxOfOrNull { it.longestStreak } ?: 0L
    }

    val consistencyScore = remember(totalMonthCompletions, uiState.habits.size) {
        if (uiState.habits.isEmpty()) 0
        else {
            val potentialTotal = uiState.habits.size * 30
            ((totalMonthCompletions.toFloat() / potentialTotal.toFloat()) * 100).toInt().coerceIn(0, 100)
        }
    }

    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthName = monthNames.getOrElse(selectedMonth) { "Month" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "PROGRESS & REPORTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Insightful Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Monthly Motivation Report Card with Share Action
        item {
            MonthlyReportCard(
                monthName = monthName,
                year = selectedYear,
                consistencyScore = consistencyScore,
                totalCompletions = totalMonthCompletions,
                activeHabitsCount = uiState.habits.size,
                longestStreak = longestActiveStreak,
                onShareReport = {
                    ShareHelper.shareMonthlyReport(
                        context = context,
                        monthName = monthName,
                        consistencyScore = consistencyScore,
                        totalCompletions = totalMonthCompletions,
                        activeHabitsCount = uiState.habits.size,
                        longestActiveStreak = longestActiveStreak,
                        userProfile = uiState.userProfile
                    )
                }
            )
        }

        // Weekly Activity Bar Chart
        item {
            WeeklyBarChart(weeklyData = weeklyData)
        }

        // Monthly Consistency Heatmap / Dot Calendar
        item {
            MonthlyCalendarGrid(
                year = selectedYear,
                month = selectedMonth,
                daysWithLogs = daysWithLogs,
                onPrevMonth = {
                    if (selectedMonth == 0) {
                        selectedMonth = 11
                        selectedYear -= 1
                    } else {
                        selectedMonth -= 1
                    }
                },
                onNextMonth = {
                    if (selectedMonth == 11) {
                        selectedMonth = 0
                        selectedYear += 1
                    } else {
                        selectedMonth += 1
                    }
                }
            )
        }

        // Per-Habit Consistency Breakdown
        item {
            Text(
                text = "HABIT BREAKDOWN & STREAKS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        if (uiState.habits.isEmpty()) {
            item {
                Text(
                    text = "No habits tracked yet. Create a habit to see streaks and stats.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.habits, key = { it.id }) { habit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Category: ${habit.category} • Total: ${habit.totalCompletions} checkoffs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${habit.currentStreak} d streak",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (habit.currentStreak >= 3) Color(0xFFF97316) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Best: ${habit.longestStreak} d",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
