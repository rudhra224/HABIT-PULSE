package com.example.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.model.Habit
import com.example.data.model.HabitLog
import com.example.ui.components.HabitCard
import com.example.ui.components.HabitListLazyColumn
import com.example.ui.theme.HabitPulseTheme
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class HabitListLazyColumnTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val habit1 = Habit(
        id = "habit_1",
        userId = "user_1",
        title = "Morning Meditation",
        category = "Health",
        colorHex = "#10B981",
        targetCount = 1L,
        unit = "times",
        currentStreak = 5L
    )

    private val habit2 = Habit(
        id = "habit_2",
        userId = "user_1",
        title = "Read Tech Articles",
        category = "Study",
        colorHex = "#6366F1",
        targetCount = 2L,
        unit = "articles",
        currentStreak = 2L
    )

    @Test
    fun habitListLazyColumn_rendersHabitItemsAndCheckboxes() {
        val habits = listOf(habit1, habit2)
        val logs = mapOf(
            habit1.id to HabitLog(id = "log_1", habitId = habit1.id, completed = false),
            habit2.id to HabitLog(id = "log_2", habitId = habit2.id, completed = true, count = 2L)
        )

        composeTestRule.setContent {
            HabitPulseTheme(themeMode = ThemeMode.LIGHT) {
                HabitListLazyColumn(
                    habits = habits,
                    logsForDate = logs,
                    onToggleHabit = {},
                    onUpdateHabitCount = { _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {},
                    onShareStreak = {}
                )
            }
        }

        // Verify LazyColumn is displayed
        composeTestRule.onNodeWithTag("habits_lazy_column").assertIsDisplayed()

        // Verify habit 1 is rendered with title and unchecked checkbox
        composeTestRule.onNodeWithTag("habit_card_habit_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Morning Meditation").assertIsDisplayed()
        composeTestRule.onNodeWithTag("habit_checkbox_habit_1")
            .assertIsDisplayed()
            .assertIsOff()

        // Verify habit 2 is rendered with title and checked checkbox
        composeTestRule.onNodeWithTag("habit_card_habit_2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read Tech Articles").assertIsDisplayed()
        composeTestRule.onNodeWithTag("habit_checkbox_habit_2")
            .assertIsDisplayed()
            .assertIsOn()
    }

    @Test
    fun habitListLazyColumn_clickCheckbox_triggersToggleCallback() {
        val habits = listOf(habit1)
        var toggledHabit: Habit? = null

        composeTestRule.setContent {
            HabitPulseTheme(themeMode = ThemeMode.LIGHT) {
                HabitListLazyColumn(
                    habits = habits,
                    logsForDate = emptyMap(),
                    onToggleHabit = { toggledHabit = it },
                    onUpdateHabitCount = { _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {},
                    onShareStreak = {}
                )
            }
        }

        // Click checkbox for habit 1
        composeTestRule.onNodeWithTag("habit_checkbox_habit_1").performClick()

        // Verify toggle callback received habit 1
        assertEquals("habit_1", toggledHabit?.id)
        assertEquals("Morning Meditation", toggledHabit?.title)
    }

    @Test
    fun habitCard_counterButtons_triggerUpdateCallback() {
        var updatedHabit: Habit? = null
        var newCountValue: Long? = null
        val log = HabitLog(id = "log_2", habitId = habit2.id, completed = false, count = 1L)

        composeTestRule.setContent {
            HabitPulseTheme(themeMode = ThemeMode.LIGHT) {
                HabitCard(
                    habit = habit2,
                    currentLog = log,
                    onToggleCompletion = {},
                    onUpdateCount = { count ->
                        updatedHabit = habit2
                        newCountValue = count
                    },
                    onEditHabit = {},
                    onDeleteHabit = {},
                    onShareStreak = {}
                )
            }
        }

        // Habit 2 has targetCount > 1, so counter button should be present
        composeTestRule.onNodeWithTag("counter_button_habit_2").performClick()

        assertEquals("habit_2", updatedHabit?.id)
        assertEquals(2L, newCountValue)
    }

    @Test
    fun habitListLazyColumn_emptyState_showsEmptyContent() {
        composeTestRule.setContent {
            HabitPulseTheme(themeMode = ThemeMode.LIGHT) {
                HabitListLazyColumn(
                    habits = emptyList(),
                    logsForDate = emptyMap(),
                    onToggleHabit = {},
                    onUpdateHabitCount = { _, _ -> },
                    onEditHabit = {},
                    onDeleteHabit = {},
                    onShareStreak = {},
                    emptyContent = {
                        Text("No habits scheduled for today")
                    }
                )
            }
        }

        composeTestRule.onNodeWithTag("habits_lazy_column").assertIsDisplayed()
        composeTestRule.onNodeWithText("No habits scheduled for today").assertIsDisplayed()
    }
}
