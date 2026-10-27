package com.example.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.local.HabitCategoryEntity
import com.example.data.model.Habit
import com.example.ui.theme.HabitPulseTheme
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class AddHabitScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleCategories = listOf(
        HabitCategoryEntity(id = "1", name = "Health"),
        HabitCategoryEntity(id = "2", name = "Work"),
        HabitCategoryEntity(id = "3", name = "Study")
    )

    @Test
    fun addHabitScreen_displaysEssentialFormFields() {
        composeTestRule.setContent {
            HabitPulseTheme(themeMode = ThemeMode.LIGHT) {
                AddHabitScreen(
                    availableCategories = sampleCategories,
                    onNavigateBack = {},
                    onSaveHabit = {}
                )
            }
        }

        // Verify root screen and top app bar
        composeTestRule.onNodeWithTag("add_habit_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_habit_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create New Habit").assertIsDisplayed()

        // Verify Material3 form input fields exist
        composeTestRule.onNodeWithTag("add_habit_name_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_habit_description_input").assertExists()
        composeTestRule.onNodeWithTag("add_habit_target_input").assertExists()
        composeTestRule.onNodeWithTag("add_habit_unit_input").assertExists()

        // Verify category chips exist
        composeTestRule.onNodeWithTag("category_chip_Health").assertExists()
        composeTestRule.onNodeWithTag("category_chip_Work").assertExists()

        // Verify save button exists
        composeTestRule.onNodeWithTag("save_habit_button").assertExists()
        composeTestRule.onNodeWithTag("top_app_bar_save_button").assertIsDisplayed()
    }

    @Test
    fun addHabitScreen_emptyTitleDisablesSave() {
        var savedHabit: Habit? = null

        composeTestRule.setContent {
            HabitPulseTheme(themeMode = ThemeMode.LIGHT) {
                AddHabitScreen(
                    availableCategories = sampleCategories,
                    onNavigateBack = {},
                    onSaveHabit = { savedHabit = it }
                )
            }
        }

        // Initially title is empty, so top save button and bottom save button are disabled
        composeTestRule.onNodeWithTag("top_app_bar_save_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("save_habit_button").performScrollTo().assertIsNotEnabled()
        assertNull(savedHabit)
    }

    @Test
    fun addHabitScreen_fillFormAndSave_triggersCallback() {
        var savedHabit: Habit? = null
        var navigatedBack = false

        composeTestRule.setContent {
            HabitPulseTheme(themeMode = ThemeMode.LIGHT) {
                AddHabitScreen(
                    availableCategories = sampleCategories,
                    onNavigateBack = { navigatedBack = true },
                    onSaveHabit = { savedHabit = it }
                )
            }
        }

        // Enter habit name
        composeTestRule.onNodeWithTag("add_habit_name_input").performTextInput("Morning Yoga")

        // Select "Work" category
        composeTestRule.onNodeWithTag("category_chip_Work").performScrollTo().performClick()

        // Click top app bar save button
        composeTestRule.onNodeWithTag("top_app_bar_save_button").performClick()

        // Assert onSaveHabit was invoked with correct details
        assertNotNull(savedHabit)
        assertEquals("Morning Yoga", savedHabit?.title)
        assertEquals("Work", savedHabit?.category)
        assertEquals(true, navigatedBack)
    }
}
