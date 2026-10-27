package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitCategoryEntity
import com.example.data.model.Habit
import com.example.ui.theme.HabitColors

/**
 * Screen with a comprehensive form using Material3 TextField components
 * to allow users to add new habits or edit existing habits.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(
    initialHabit: Habit? = null,
    availableCategories: List<HabitCategoryEntity> = emptyList(),
    onNavigateBack: () -> Unit,
    onSaveHabit: (Habit) -> Unit,
    onAddCategory: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val isEditing = initialHabit != null
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Form states
    var title by remember { mutableStateOf(initialHabit?.title.orEmpty()) }
    var titleError by remember { mutableStateOf<String?>(null) }

    var description by remember { mutableStateOf("") }
    var category by remember {
        mutableStateOf(
            initialHabit?.category ?: availableCategories.firstOrNull()?.name ?: "Health"
        )
    }
    var selectedColor by remember { mutableStateOf(initialHabit?.colorHex ?: HabitColors[0]) }
    var targetCountText by remember {
        mutableStateOf((initialHabit?.targetCount ?: 1L).toString())
    }
    var targetCountError by remember { mutableStateOf<String?>(null) }
    var unit by remember { mutableStateOf(initialHabit?.unit ?: "times") }
    var frequency by remember { mutableStateOf(initialHabit?.frequency ?: "daily") }

    // Reminder push notification settings
    var reminderEnabled by remember {
        mutableStateOf(!initialHabit?.reminderTime.isNullOrBlank())
    }
    var reminderTime by remember {
        mutableStateOf(initialHabit?.reminderTime ?: "08:30")
    }
    var reminderTimeError by remember { mutableStateOf<String?>(null) }

    // Inline category creation
    var showNewCategoryInput by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val presetUnits = listOf("times", "mins", "pages", "glasses", "steps", "km", "pomodoros")
    val frequencyOptions = listOf(
        "daily" to "Daily",
        "weekdays" to "Weekdays",
        "weekends" to "Weekends",
        "custom" to "Custom"
    )

    fun validateAndSave() {
        var isValid = true

        if (title.trim().isBlank()) {
            titleError = "Habit title cannot be empty"
            isValid = false
        } else if (title.trim().length > 60) {
            titleError = "Title must be 60 characters or fewer"
            isValid = false
        } else {
            titleError = null
        }

        val targetNum = targetCountText.toLongOrNull()
        if (targetNum == null || targetNum <= 0L) {
            targetCountError = "Target must be at least 1"
            isValid = false
        } else {
            targetCountError = null
        }

        if (reminderEnabled) {
            val parts = reminderTime.split(":")
            val validHour = parts.getOrNull(0)?.toIntOrNull() in 0..23
            val validMin = parts.getOrNull(1)?.toIntOrNull() in 0..59
            if (parts.size != 2 || !validHour || !validMin) {
                reminderTimeError = "Enter valid time in HH:MM format (e.g. 08:30)"
                isValid = false
            } else {
                reminderTimeError = null
            }
        } else {
            reminderTimeError = null
        }

        if (isValid) {
            val count = targetNum ?: 1L
            val finalHabit = (initialHabit ?: Habit()).copy(
                title = title.trim(),
                category = category.trim().ifBlank { "General" },
                colorHex = selectedColor,
                targetCount = count,
                unit = unit.trim().ifBlank { "times" },
                frequency = frequency,
                reminderTime = if (reminderEnabled) reminderTime.trim() else ""
            )
            onSaveHabit(finalHabit)
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("add_habit_screen"),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Habit" else "Create New Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_habit_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { validateAndSave() },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("top_app_bar_save_button"),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Habit Basics
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "HABIT BASICS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Habit Name TextField
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (titleError != null && it.isNotBlank()) {
                                titleError = null
                            }
                        },
                        label = { Text("Habit Name *") },
                        placeholder = { Text("e.g., Morning Meditation, Drink Water") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (title.isNotEmpty()) {
                                IconButton(onClick = { title = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear name"
                                    )
                                }
                            }
                        },
                        isError = titleError != null,
                        supportingText = {
                            if (titleError != null) {
                                Text(
                                    text = titleError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Text(
                                    text = "${title.length}/60 characters",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_habit_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description / Motivation TextField
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Motivation & Notes (Optional)") },
                        placeholder = { Text("Why do you want to build this habit? How will it help you?") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_habit_description_input")
                    )
                }
            }

            // Section 2: Category & Theme Color
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = if (showNewCategoryInput) "Cancel" else "+ New Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { showNewCategoryInput = !showNewCategoryInput }
                                .padding(4.dp)
                                .testTag("toggle_new_category_button")
                        )
                    }

                    // Optional inline category creation TextField
                    if (showNewCategoryInput) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                placeholder = { Text("Category name") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_category_input")
                            )

                            Button(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        val trimmed = newCategoryName.trim()
                                        onAddCategory(trimmed, selectedColor)
                                        category = trimmed
                                        newCategoryName = ""
                                        showNewCategoryInput = false
                                    }
                                },
                                enabled = newCategoryName.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("add_category_button")
                            ) {
                                Text("Add")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category FilterChips
                    val allCats = if (availableCategories.isEmpty()) {
                        listOf("Health", "Work", "Study", "Fitness", "Mindfulness")
                    } else {
                        availableCategories.map { it.name }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        allCats.forEach { catName ->
                            val isSelected = category.equals(catName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = catName },
                                label = { Text(catName) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("category_chip_$catName")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Color Palette Swatches
                    Text(
                        text = "THEME COLOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HabitColors.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = selectedColor.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                                    .testTag("color_swatch_$hex"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected color",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Target Goal & Frequency
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DAILY TARGET & UNIT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Daily Target Count TextField
                        OutlinedTextField(
                            value = targetCountText,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    targetCountText = it
                                    targetCountError = null
                                }
                            },
                            label = { Text("Daily Count *") },
                            placeholder = { Text("1") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.TrackChanges,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            isError = targetCountError != null,
                            supportingText = targetCountError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_habit_target_input")
                        )

                        // Target Unit TextField
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit") },
                            placeholder = { Text("times") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_habit_unit_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset unit suggestion chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presetUnits.forEach { presetUnit ->
                            val isSelected = unit.equals(presetUnit, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { unit = presetUnit },
                                label = { Text(presetUnit, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("unit_chip_$presetUnit")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Frequency Selector
                    Text(
                        text = "FREQUENCY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        frequencyOptions.forEach { (freqKey, freqLabel) ->
                            val isSelected = frequency == freqKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { frequency = freqKey },
                                label = { Text(freqLabel) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("frequency_chip_$freqKey")
                            )
                        }
                    }
                }
            }

            // Section 4: Daily Push Notification Reminders
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Daily Reminder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Nudge to protect your streak",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            modifier = Modifier.testTag("reminder_toggle_switch")
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        // Reminder Time TextField
                        OutlinedTextField(
                            value = reminderTime,
                            onValueChange = {
                                reminderTime = it
                                reminderTimeError = null
                            },
                            label = { Text("Reminder Time (24h format)") },
                            placeholder = { Text("08:30") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            isError = reminderTimeError != null,
                            supportingText = {
                                if (reminderTimeError != null) {
                                    Text(reminderTimeError ?: "", color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("App will schedule a local notification at this time")
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_habit_reminder_input")
                        )
                    }
                }
            }

            // Section 5: Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { validateAndSave() },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_habit_button")
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Save Changes" else "Create Habit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("cancel_habit_button")
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
