package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import com.example.data.model.Habit
import com.example.data.model.HabitLog

fun parseHexColor(hex: String, defaultColor: Color = Color(0xFF6366F1)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun StreakFlameBadge(
    streakCount: Long,
    modifier: Modifier = Modifier
) {
    val isHot = streakCount >= 3
    val flameColor = if (isHot) Color(0xFFF97316) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val bgColor = if (isHot) Color(0xFFFEF3C7).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Streak flame",
                tint = flameColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$streakCount d",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHot) Color(0xFFF97316) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    currentLog: HabitLog?,
    onToggleCompletion: () -> Unit,
    onUpdateCount: (Long) -> Unit,
    onEditHabit: () -> Unit,
    onDeleteHabit: () -> Unit,
    onShareStreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitColor = parseHexColor(habit.colorHex)
    val isCompleted = currentLog?.completed == true
    val currentCount = currentLog?.count ?: 0L
    var showMenu by remember { mutableStateOf(false) }

    val cardBg by animateColorAsState(
        if (isCompleted) habitColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        label = "cardBg"
    )
    val borderColor by animateColorAsState(
        if (isCompleted) habitColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Category & options header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category indicator dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(habitColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = habit.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = habitColor,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StreakFlameBadge(streakCount = habit.currentStreak)
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("habit_menu_button_${habit.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Habit options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Goal") },
                                onClick = {
                                    showMenu = false
                                    onEditHabit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Streak") },
                                onClick = {
                                    showMenu = false
                                    onShareStreak()
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Habit", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDeleteHabit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main row: Material 3 Checkbox for completion + title & progress + count stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox for completion
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onToggleCompletion() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = habitColor,
                        checkmarkColor = Color.White,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .testTag("habit_checkbox_${habit.id}")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleCompletion() }
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val progressSummary = if (habit.targetCount > 1L) {
                        "$currentCount / ${habit.targetCount} ${habit.unit}"
                    } else {
                        if (isCompleted) "Completed for today" else "Tap checkbox to complete"
                    }
                    Text(
                        text = progressSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) habitColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stepper controls for quantity/count habits (e.g. 8 glasses of water)
                if (habit.targetCount > 1L) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    val next = (currentCount - 1).coerceAtLeast(0L)
                                    onUpdateCount(next)
                                },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease count",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isCompleted) habitColor else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    val next = currentCount + 1
                                    onUpdateCount(next)
                                }
                                .testTag("counter_button_${habit.id}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase count",
                                    tint = if (isCompleted) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * LazyColumn to display the list of habits added by the user with checkboxes for completion.
 */
@Composable
fun HabitListLazyColumn(
    habits: List<Habit>,
    logsForDate: Map<String, HabitLog>,
    onToggleHabit: (Habit) -> Unit,
    onUpdateHabitCount: (Habit, Long) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (String) -> Unit,
    onShareStreak: (Habit) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    headerContent: @Composable () -> Unit = {},
    emptyContent: @Composable () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.testTag("habits_lazy_column"),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            headerContent()
        }
        if (habits.isEmpty()) {
            item {
                emptyContent()
            }
        } else {
            items(habits, key = { it.id }) { habit ->
                val currentLog = logsForDate[habit.id]
                HabitCard(
                    habit = habit,
                    currentLog = currentLog,
                    onToggleCompletion = { onToggleHabit(habit) },
                    onUpdateCount = { newCount -> onUpdateHabitCount(habit, newCount) },
                    onEditHabit = { onEditHabit(habit) },
                    onDeleteHabit = { onDeleteHabit(habit.id) },
                    onShareStreak = { onShareStreak(habit) }
                )
            }
        }
    }
}
